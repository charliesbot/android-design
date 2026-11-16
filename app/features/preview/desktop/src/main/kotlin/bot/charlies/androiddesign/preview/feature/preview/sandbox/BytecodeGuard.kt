package bot.charlies.androiddesign.preview.feature.preview.sandbox

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Inspects a compiled JVM classfile's constant pool before loading or initializing any class,
 * rejecting disallowed classes, thread-pool coroutine dispatchers, direct `MethodHandles` use, and
 * reflective `java.lang.Class` lookups.
 */
internal object BytecodeGuard {
  private const val CAFEBABE = 0xCAFEBABE.toInt()
  private const val TAG_UTF8 = 1
  private const val TAG_INTEGER = 3
  private const val TAG_FLOAT = 4
  private const val TAG_LONG = 5
  private const val TAG_DOUBLE = 6
  private const val TAG_CLASS = 7
  private const val TAG_STRING = 8
  private const val TAG_FIELDREF = 9
  private const val TAG_METHODREF = 10
  private const val TAG_INTERFACE_METHODREF = 11
  private const val TAG_NAME_AND_TYPE = 12
  private const val TAG_METHOD_HANDLE = 15
  private const val TAG_METHOD_TYPE = 16
  private const val TAG_DYNAMIC = 17
  private const val TAG_INVOKE_DYNAMIC = 18
  private const val TAG_MODULE = 19
  private const val TAG_PACKAGE = 20

  private sealed interface CpEntry {
    data class Utf8(val text: String) : CpEntry

    data class ClassRef(val nameIndex: Int) : CpEntry

    data class MemberRef(val classIndex: Int, val nameAndTypeIndex: Int) : CpEntry

    data class NameAndType(val nameIndex: Int, val descriptorIndex: Int) : CpEntry

    data class MethodHandleRef(val refIndex: Int) : CpEntry

    data class DescriptorRef(val index: Int) : CpEntry

    data class DynamicRef(val nameAndTypeIndex: Int) : CpEntry

    data object Other : CpEntry
  }

  fun verify(className: String, bytes: ByteArray, siblingClasses: Set<String>): List<String> {
    val violations = LinkedHashSet<String>()
    val buf = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
    if (bytes.size < 10 || buf.getInt() != CAFEBABE) {
      return listOf("Invalid classfile header for $className")
    }
    buf.getShort() // minor
    buf.getShort() // major
    val cpCount = buf.getShort().toInt() and 0xFFFF
    val entries = arrayOfNulls<CpEntry>(cpCount)
    var index = 1
    while (index < cpCount) {
      val tag = buf.get().toInt() and 0xFF
      when (tag) {
        TAG_UTF8 -> {
          val len = buf.getShort().toInt() and 0xFFFF
          val utf = ByteArray(len)
          buf.get(utf)
          entries[index] = CpEntry.Utf8(String(utf, Charsets.UTF_8))
          index++
        }
        TAG_INTEGER,
        TAG_FLOAT -> {
          buf.getInt()
          entries[index] = CpEntry.Other
          index++
        }
        TAG_LONG,
        TAG_DOUBLE -> {
          buf.getLong()
          entries[index] = CpEntry.Other
          index += 2
        }
        TAG_CLASS -> {
          entries[index] = CpEntry.ClassRef(buf.getShort().toInt() and 0xFFFF)
          index++
        }
        TAG_STRING,
        TAG_MODULE,
        TAG_PACKAGE -> {
          buf.getShort()
          entries[index] = CpEntry.Other
          index++
        }
        TAG_METHOD_TYPE -> {
          entries[index] = CpEntry.DescriptorRef(buf.getShort().toInt() and 0xFFFF)
          index++
        }
        TAG_FIELDREF,
        TAG_METHODREF,
        TAG_INTERFACE_METHODREF -> {
          val ci = buf.getShort().toInt() and 0xFFFF
          val nti = buf.getShort().toInt() and 0xFFFF
          entries[index] = CpEntry.MemberRef(ci, nti)
          index++
        }
        TAG_NAME_AND_TYPE -> {
          val ni = buf.getShort().toInt() and 0xFFFF
          val di = buf.getShort().toInt() and 0xFFFF
          entries[index] = CpEntry.NameAndType(ni, di)
          index++
        }
        TAG_METHOD_HANDLE -> {
          buf.get() // reference_kind
          entries[index] = CpEntry.MethodHandleRef(buf.getShort().toInt() and 0xFFFF)
          index++
        }
        TAG_DYNAMIC,
        TAG_INVOKE_DYNAMIC -> {
          buf.getShort() // bootstrap_method_attr_index
          entries[index] = CpEntry.DynamicRef(buf.getShort().toInt() and 0xFFFF)
          index++
        }
        else -> return listOf("Unsupported constant pool tag $tag in $className")
      }
    }

    fun utf8(i: Int): String = (entries.getOrNull(i) as? CpEntry.Utf8)?.text.orEmpty()
    fun classDot(i: Int): String? {
      val ref = entries.getOrNull(i) as? CpEntry.ClassRef ?: return null
      return internalToDotClass(utf8(ref.nameIndex))
    }

    val handleTargets =
      entries.filterIsInstance<CpEntry.MethodHandleRef>().map { it.refIndex }.toSet()

    fun checkClassAllowed(dotName: String?) {
      if (dotName != null && !SandboxPolicy.isAllowedClass(dotName, siblingClasses)) {
        violations += "Disallowed class reference '$dotName' in $className"
      }
    }

    fun checkDescriptor(descriptor: String) {
      extractDescriptorClasses(descriptor).forEach(::checkClassAllowed)
    }

    entries.forEachIndexed { entryIndex, entry ->
      when (entry) {
        is CpEntry.ClassRef -> {
          val dotName = internalToDotClass(utf8(entry.nameIndex))
          if (dotName in SandboxPolicy.DENIED_CLASS_REFS) {
            violations += "Direct invoke API class '$dotName' is denied in explorations"
          } else {
            checkClassAllowed(dotName)
          }
        }
        is CpEntry.NameAndType -> checkDescriptor(utf8(entry.descriptorIndex))
        is CpEntry.DescriptorRef -> checkDescriptor(utf8(entry.index))
        is CpEntry.MemberRef -> {
          val owner = classDot(entry.classIndex)
          checkClassAllowed(owner)
          val nt = entries.getOrNull(entry.nameAndTypeIndex) as? CpEntry.NameAndType
          val memberName = nt?.let { utf8(it.nameIndex) }.orEmpty()
          nt?.let { checkDescriptor(utf8(it.descriptorIndex)) }
          if (memberName in SandboxPolicy.DENIED_COROUTINE_MEMBERS) {
            violations += "Thread-pool dispatcher '$memberName' is denied in explorations"
          }
          if (owner in SandboxPolicy.DENIED_INVOKE_DIRECT_OWNERS) {
            violations += "Direct invoke API call '$owner.$memberName' is denied in explorations"
          }
          if (
            owner != null &&
              owner.startsWith("java.lang.invoke.") &&
              (entryIndex !in handleTargets || owner !in SandboxPolicy.ALLOWED_BOOTSTRAP_OWNERS)
          ) {
            violations += "Method handle or invoke target '$owner.$memberName' is denied"
          }
          if (owner == "java.lang.Class" && memberName in SandboxPolicy.DENIED_CLASS_METHODS) {
            violations += "Reflective Class.$memberName() is denied in explorations"
          }
        }
        else -> Unit
      }
    }

    return violations.toList()
  }

  internal fun internalToDotClass(raw: String): String? {
    var s = raw
    while (s.startsWith('[')) s = s.substring(1)
    if (s.startsWith('L') && s.endsWith(';')) {
      s = s.substring(1, s.length - 1)
    } else if (raw.startsWith('[') && s.length == 1) {
      return null // Primitive array like `[I` or `[F`.
    }
    return s.replace('/', '.')
  }

  private fun extractDescriptorClasses(descriptor: String): List<String> {
    val classes = mutableListOf<String>()
    var i = 0
    while (i < descriptor.length) {
      if (descriptor[i] == 'L') {
        val end = descriptor.indexOf(';', i + 1)
        if (end < 0) break
        classes += descriptor.substring(i + 1, end).replace('/', '.')
        i = end + 1
      } else {
        i++
      }
    }
    return classes
  }
}

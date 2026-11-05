package android.annotation

@Target(
  AnnotationTarget.FILE,
  AnnotationTarget.CLASS,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY,
  AnnotationTarget.VALUE_PARAMETER,
  AnnotationTarget.LOCAL_VARIABLE,
)
annotation class SuppressLint(vararg val value: String)

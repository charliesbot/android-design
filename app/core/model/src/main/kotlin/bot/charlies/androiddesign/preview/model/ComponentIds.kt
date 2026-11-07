package bot.charlies.androiddesign.preview.model

/**
 * The components of [M3ExpressiveCatalog], typed. Each entry's name is its wire name, the
 * `component` a proposal node gives. Code that does something per component switches over these
 * with an exhaustive `when`, so a new catalog component fails to compile until every switch handles
 * it. A model test keeps the entries and the catalog's components equal.
 */
enum class M3Component {
  Column,
  Row,
  Box,
  Grid,
  FlexBox,
  Spacer,
  Divider,
  Shape,
  Text,
  Icon,
  Image,
  Carousel,
  Scaffold,
  TopAppBar,
  NavigationBar,
  NavigationBarItem,
  Tabs,
  Tab,
  NavigationRail,
  NavigationRailItem,
  FloatingToolbar,
  DockedToolbar,
  Card,
  List,
  ListItem,
  Button,
  ToggleButton,
  ButtonGroup,
  IconButton,
  SplitButton,
  FloatingActionButton,
  FabMenu,
  FabMenuItem,
  AssistChip,
  FilterChip,
  InputChip,
  SuggestionChip,
  ChipGroup,
  Switch,
  Checkbox,
  Slider,
  RangeSlider,
  RadioGroup,
  RadioButton,
  TextField,
  SearchBar,
  LoadingIndicator,
  ProgressIndicator,
  BottomSheet,
  Dialog,
  DatePicker,
  TimePicker,
  Menu,
  MenuGroup,
  MenuItem,
  SideSheet,
  SearchView,
  Tooltip,
  Snackbar;

  val wire: String
    get() = name

  companion object {
    private val byWire = entries.associateBy { it.wire }

    fun fromWire(wire: String): M3Component? = byWire[wire]

    /** The component [wire] names. Validation rejects unknown names, so one here is a bug. */
    fun of(wire: String): M3Component =
      fromWire(wire) ?: error("$wire is not a ${M3ExpressiveCatalog.KEY} component")
  }
}

/** The components of [WearCatalog], typed like [M3Component]. */
enum class WearComponent {
  Column,
  Row,
  Box,
  Spacer,
  Screen,
  ListHeader,
  ListSubHeader,
  Button,
  CompactButton,
  IconButton,
  TextButton,
  EdgeButton,
  ButtonGroup,
  IconToggleButton,
  TextToggleButton,
  SwitchButton,
  CheckboxButton,
  RadioButton,
  Text,
  Icon,
  Image,
  Card,
  TitleCard,
  AppCard,
  Slider,
  Stepper,
  CircularProgressIndicator,
  LinearProgressIndicator,
  SegmentedCircularProgressIndicator,
  AlertDialog,
  ConfirmationDialog;

  val wire: String
    get() = name

  companion object {
    private val byWire = entries.associateBy { it.wire }

    fun fromWire(wire: String): WearComponent? = byWire[wire]

    /** The component [wire] names. Validation rejects unknown names, so one here is a bug. */
    fun of(wire: String): WearComponent =
      fromWire(wire) ?: error("$wire is not a ${WearCatalog.KEY} component")
  }
}

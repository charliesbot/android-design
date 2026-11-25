package bot.charlies.androiddesign.preview.model

/** Canonical wire names for Material 3 components on [PhoneSurface]. */
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
  Tabs,
  Tab,
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
  Scrim,
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

    /** The component [wire] names, failing loudly when unknown. */
    fun of(wire: String): M3Component =
      fromWire(wire) ?: error("$wire is not a ${PhoneSurface.key} component")
  }
}

/** Canonical wire names for Wear OS components on [WearSurface]. */
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

    /** The component [wire] names, failing loudly when unknown. */
    fun of(wire: String): WearComponent =
      fromWire(wire) ?: error("$wire is not a ${WearSurface.key} component")
  }
}

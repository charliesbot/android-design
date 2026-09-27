# Position indicator
Source: https://developer.android.com/design/ui/wear/guides/m2-5/components/position-indicator

The PositionIndicator component displays the user’s location in a list or range value.

Use position indicators in list or for other contexts when you can use the rotating side button (RSB) to scroll, adjust settings, control volume, or do other actions.

## Anatomy

A. Track
B. Indicator

## Usage

See the following examples of position indicators.

## Large screens

### **Responsive behavior**

The position indicator will remain the same size across screen sizes, this means the angle of the curve will change slightly to match.

**Position indicator**

The arc of the indicator is always 50 dp high (doesn’t scale proportionally) meaning only the degree changes as you go up in size.

It’s possible to manually adjust the margin down to 2 dp to match SysUI. The height will remain fixed at 50 dp.

**Control/RSB indicator**

The arc of the indicator is always 76 dp high (doesn’t scale proportionally) meaning only the degree changes as you go up in size.

It’s possible to:
- Manually adjust the margin down to 2 dp to match SysUI. The height will remain fixed at 50 dp.
- Have the indicator sit on the left or right side of the screen.

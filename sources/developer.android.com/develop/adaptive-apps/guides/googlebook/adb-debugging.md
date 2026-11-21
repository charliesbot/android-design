# ADB debugging on Googlebook
Source: https://developer.android.com/develop/adaptive-apps/guides/googlebook/adb-debugging

You can connect to a Googlebook using Android Debug Bridge (adb) over Wi-Fi or USB to install, run, and debug your Android apps directly on Googlebook hardware.

## Supported connection methods by device

All Googlebook models support wireless debugging over Wi-Fi. USB debugging support and port availability vary by model:
| Device  | Method
| **HP Googlebook 14**  | Wi-Fi, USB (left port only)
| **XPS Googlebook**  | Wi-Fi, USB (left port only)
| **Acer Googlebook 14**  | Wi-Fi, USB coming soon (all ports)
| **ASUS Googlebook 14**  | Wi-Fi, USB coming soon (all ports)
| **Lenovo Googlebook 15**  | Wi-Fi, USB coming soon (all ports)

## Connect to a Googlebook

Choose a connection method supported by your Googlebook model:
- **Wi-Fi debugging** (all models): Enable **Developer options** and **Wireless debugging** on your Googlebook, and then pair from an external workstation over the same Wi-Fi network or directly from the Googlebook's on-device Linux terminal using a pairing code or QR code. For step-by-step setup instructions, see Connect to a device over Wi-Fi.
- **USB debugging:** On models with active USB debugging support, enable **USB debugging** in **Developer options** and connect your USB cable to the supported port listed in the preceding table.

## Additional resources
- Build adaptive apps for Googlebook
- Run apps on a hardware device
- Test different screen and window sizes

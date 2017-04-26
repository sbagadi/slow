# slow

This is an Android Wear watch face app. The watch face is an analog style dial
with 24 hours in a single rotation instead of the regular 12 hours.

This project consits of the following three modues:

### mobile

The mobile module is an Android wearable companion app. The app consists of a
Wearble Companion configuration `Activity`. The `Activity` consists of a preview
of the watch face and lets the user set configurations for the watch face.

### wear

The wear module consists of the watch face service and the configuration
activities.

### common

The common module consists of code used by both the mobile and wear modules.

## Future plans

- Add additional themes:
    - A day and night theme.
    - Couple of other color based themes.
- Add a indicator for Sun rise and Sun set times.
- Add additional widgets:
    - Steps widget.
    - Weather widget.
- Other performance improvements.

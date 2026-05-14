# Unused Legacy Code

Current Android build modules are defined in `settings.gradle`:

```gradle
include ':app', ':chart', ':listview'
```

The folders below are kept as old project/reference code, but they are not compiled by the current app unless `settings.gradle` and module dependencies are changed:

- `aslibrary/`
- `chartview/`

The C# `FileServer` project under `app/src/main/Csharp/` is a separate PC helper tool, not an Android Gradle module.

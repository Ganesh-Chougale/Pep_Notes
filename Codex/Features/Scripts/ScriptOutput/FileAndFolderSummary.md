```
main
├── AndroidManifest.xml
├── java/
│   └── com/
│       └── horizone/
│           └── pep_notes/
│               ├── data/
│               │   ├── db/
│               │   │   ├── LabelDao.kt
│               │   │   ├── NoteDao.kt
│               │   │   ├── PepDatabase.kt
│               │   │   └── PersonDao.kt
│               │   ├── model/
│               │   │   ├── Note.kt
│               │   │   ├── NoteLabel.kt
│               │   │   ├── Person.kt
│               │   │   └── PersonLabel.kt
│               │   └── repository/
│               │       ├── LabelRepository.kt
│               │       ├── NoteRepository.kt
│               │       └── PersonRepository.kt
│               ├── MainActivity.kt
│               ├── ui/
│               │   ├── export/
│               │   │   └── ExportImportScreen.kt
│               │   ├── labels/
│               │   │   ├── NoteLabelManagerScreen.kt
│               │   │   └── PersonLabelManagerScreen.kt
│               │   ├── nav/
│               │   │   ├── AppNavHost.kt
│               │   │   └── NavRoutes.kt
│               │   ├── notes/
│               │   │   ├── components/
│               │   │   │   ├── NoteCard.kt
│               │   │   │   └── NoteLabelChip.kt
│               │   │   ├── NoteEditScreen.kt
│               │   │   └── PersonNotesScreen.kt
│               │   ├── people/
│               │   │   ├── components/
│               │   │   │   ├── PersonCard.kt
│               │   │   │   └── PersonLabelChip.kt
│               │   │   ├── PeopleListScreen.kt
│               │   │   └── PersonEditScreen.kt
│               │   └── theme/
│               │       ├── Color.kt
│               │       ├── Theme.kt
│               │       └── Type.kt
│               ├── util/
│               │   ├── DateUtil.kt
│               │   ├── FileUtil.kt
│               │   └── JsonUtil.kt
│               └── viewmodel/
│                   ├── LabelViewModel.kt
│                   ├── NoteViewModel.kt
│                   └── PersonViewModel.kt
└── res/
    ├── drawable/
    │   ├── ic_launcher_background.xml
    │   └── ic_launcher_foreground.xml
    ├── mipmap-anydpi-v26/
    │   ├── ic_launcher_round.xml
    │   └── ic_launcher.xml
    ├── mipmap-hdpi/
    │   ├── ic_launcher_round.webp
    │   └── ic_launcher.webp
    ├── mipmap-mdpi/
    │   ├── ic_launcher_round.webp
    │   └── ic_launcher.webp
    ├── mipmap-xhdpi/
    │   ├── ic_launcher_round.webp
    │   └── ic_launcher.webp
    ├── mipmap-xxhdpi/
    │   ├── ic_launcher_round.webp
    │   └── ic_launcher.webp
    ├── mipmap-xxxhdpi/
    │   ├── ic_launcher_round.webp
    │   └── ic_launcher.webp
    ├── values/
    │   ├── colors.xml
    │   ├── strings.xml
    │   └── themes.xml
    └── xml/
        ├── backup_rules.xml
        └── data_extraction_rules.xml
```
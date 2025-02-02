# Notes App

 A simple and functional Android app in Kotlin for managing notes. The app allows users to create, update, delete, and search for notes, with additional features like categories, importance levels, and reminders.

### Key Features

- **Create Notes**: Users can add new notes that have a title, content, category and an importance level.
- **Edit Notes**: Ability to edit existing notes.
- **Delete Notes**: Remove notes with confirmation to avoid accidental deletions.
- **Set Reminders**: Notes with the highest importance level allow user to set reminders for them.
- **Search notes**: App allows user to search their note by a phrase from title, content and by category and.
- **Categories**: Organize notes into predefined categories: Work, Personal, Study.

### Architecture

Our project uses the `Model-View-ViewModel` architecture which is a pattern used to separate the logic of an application into three components:
- **Model**: Represents the data and business logic of the app.
- **View**: Displays the UI and interacts with the user.
- **ViewModel**: Acts as a bridge between the Model and View, handling the logic and preparing data for the View.

We use `MVVM` to keep things organized and separate the concerns between data handling and UI. The Model part is represented by the `Note` data class which stores details like title description category and importance level, the `View` is made up of fragments which display the UI and let users interact with the notes like adding editing or searching them, the `ViewModel` is the middle layer which handles the logic it communicates with the repository to fetch or modify the notes data and the `Repository` is where the database operations happen it interacts with the Room database to manage note data.

### Preview

https://github.com/user-attachments/assets/e183d640-52c1-4cc7-a397-b49164cfa7e8


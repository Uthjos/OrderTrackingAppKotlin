# Order Tracking System (ICS 372-01)

A Kotlin/JavaFX desktop application for managing restaurant orders.

## Project summary

- Language: Kotlin/Java 21
- Build tool: Gradle
- UI: JavaFX (javafx.controls, javafx.fxml)

## Features

- Display order overview, with further details on click
- Edit the status of an order (Waiting -> In Progress -> Completed)
- Cancel or re-submit cancelled orders
- Import orders from JSON or XML files
- Save order states on exit and load saved orders on startup

## Project layout

- Source: `src/main/kotlin`
- FXML/UI: `src/main/resources/org/metrostate/ics/ordertrackingappkotlin/`
- Orders file handling directories: `src/main/orderFiles/` (contains `importOrders/`, `savedOrders/`)
- Tests: `src/test/kotlin/org/metrostate/ics/ordertrackingappkotlin/`

Note: The repository purposely keeps placeholder files (`spaceHolder.txt`) in the `orderFiles` directories so Git tracks the directories when actual JSON/XML orders are ignored.

## Run

Run within an IDE that supports Gradle (e.g., IntelliJ IDEA) by running the `Launcher.kt` main class.

## Using the application

1. On first startup, the application will not have any orders loaded.
2. Import one or more orders by pasting a validly formatted order file (JSON or XML) into the `src/main/orderFiles/importOrders/` directory, which will automatically populate the order list.
    - NOTE: Files in this directory will be deleted on application exit.
3. Click on an order in the list to view its details, and edit its status or cancel/un-cancel it using the provided buttons.
    - Return to the main screen by using the back arrow button.
4. Filter orders by status and/or type using the dropdown menus at the top.
5. On exit:
    - All current orders will be saved to `src/main/orderFiles/savedOrders/` as JSON files.
    - On the next startup, these saved orders will be automatically loaded and then cleared from the directory.
    - All order files in `src/main/orderFiles/importOrders/` will be deleted (except `.txt` placeholder files).

## Requirements and Use Case Document
https://docs.google.com/document/d/1ZZ_qqwHPmVuCYHXN2_n7UoWFspIYIPRkLV6WVAtXNIA/edit?tab=t.0

## System Loads Orders on Import Directory Update Sequence Diagram
![Project 2 Sequence Diagram](src/main/java/design/SystemLoadsOrdersOnStartup.png)
## System Saves Orders on Exit Sequence Diagram
![Project 2 Sequence Diagram](src/main/java/design/StaffSavesOrdersOnExit.png)

## UML Class Diagram
![Project 2 UML](src/main/java/design/ClassUMLDiagram.png)


## Dependencies

- JavaFX 21
- org.json:json
- JUnit Jupiter (for tests)
- Mockito (tests)

## Authors

- Joseph Murtha
- Rocky Xiong
- Ashley Zenzola
- Aidan Mahlberg

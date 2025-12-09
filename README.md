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
- Adjust tips for dine-in orders (server and kitchen tips)
- Import orders from JSON or XML files with automatic file monitoring
- Automatic order persistence and crash recovery
- Daily order archival for administrative purposes

## Project layout

- Source: `src/main/kotlin` (Kotlin), `src/main/java` (Java components)
- FXML/UI: `src/main/resources/org/metrostate/ics/ordertrackingappkotlin/`
- Orders file handling directories: `src/main/orderFiles/`
  - `importOrders/` - Drop new order files here for automatic import
  - `savedOrders/` - Active order state persistence (auto-managed)
  - `historyOrders/` - Daily archives created on manual exit
- Tests: `src/test/kotlin/org/metrostate/ics/ordertrackingappkotlin/`

Note: The repository purposely keeps placeholder files (`spaceHolder.txt`) in the `orderFiles` directories so Git tracks the directories when actual JSON/XML orders are ignored.

## Run

Run within an IDE that supports Gradle (e.g., IntelliJ IDEA) by running the `Launcher.kt` main class.

## Using the application

### Importing Orders
1. Place validly formatted order files (JSON or XML) into `src/main/orderFiles/importOrders/`
2. The application automatically detects and loads new files within seconds, but will take longer for batch imports
3. Orders immediately appear in the main view and are saved to `savedOrders/`
4. After initial batch loading, imported files are deleted from `importOrders/`

### Managing Orders
1. Click on any order tile to view full details
2. Update order status using action buttons:
   - **Start** - Moves order from Waiting to In Progress
   - **Complete** - Marks order as completed
   - **Cancel** - Cancels the order (with confirmation dialog)
   - **Resubmit** - Restores cancelled order to Waiting status
3. For dine-in orders, use **Adjust Tip** to set server and kitchen tips
4. Use the back arrow to return to the main view
5. Filter orders by status and/or type using dropdown menus

### Order Persistence & Recovery

**During Runtime:**
- Orders are automatically saved to `savedOrders/` when:
  - First imported from `importOrders/`
  - Status is changed (Start, Complete, Cancel, Resubmit)
  - Tips are adjusted (dine-in orders only)
- This provides crash recovery - if the app crashes, all orders persist in `savedOrders/`

**On Startup:**
- The application loads all orders from `savedOrders/` (if any exist from previous crash)
- After loading, `savedOrders/` is cleared
- Then begins monitoring `importOrders/` for new files

**On Manual Exit (Shutdown):**
- This simulates a restaurant closing at end of day with orders archived for system administrators and wiping the day's orders
- All active orders are archived to `historyOrders/YYYY-MM-DD/` with today's date
- Both `savedOrders/` and `importOrders/` directories are cleaned up

### Directory Behavior Summary
| Directory | Purpose | When Files Added | When Files Deleted |
|-----------|---------|------------------|-------------------|
| `importOrders/` | Drop zone for new orders | User places files | After batch load or on exit |
| `savedOrders/` | Active order state | On import/update | On startup or manual exit |
| `historyOrders/` | Daily archives | On manual exit | Never (admin managed) |

## Refactoring report, use case, and requirements 
https://docs.google.com/document/d/1CMUD7VJLF-AriUZpSTu1kODVhdRGDYPKBn9hZHDS9Os/edit?usp=sharing

## System Loads Orders on Import Directory Update Sequence Diagram
![Project 2 Sequence Diagram](src/main/java/design/SystemLoadsOrdersOnStartup.png)
## System Saves Orders on Exit Sequence Diagram
![Project 2 Sequence Diagram](src/main/java/design/StaffSavesOrdersOnExit.png)

## UML Class Diagram
![Project 3 UML](src/main/java/design/ClassUMLDiagram.png)


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

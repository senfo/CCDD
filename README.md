# CCDD
Core Flight System (CFS) Command and Data Dictionary (CCDD) utility

CCDD is a software tool for managing the command and telemetry data for CFS and CFS applications.  CCDD is written in Java™ and interacts with a PostgreSQL database, so it can be used on any operating system that supports the Java Runtime Environment (JRE) and PostgreSQL.  CCDD is released as open source software under the NASA Open Source Software Agreement, version 1.3, and is hosted on GitHub.

The CCDD application uses tables, similar to a spreadsheet, to display and allow manipulation of telemetry data structures, command information, and other data pertinent to a CFS project.  The data is stored in a PostgreSQL database for manipulation and data security.  The PostgreSQL database server can be run locally or centralized on a remote host for easier access by multiple users.  Data can be imported into the application from files in comma-separated values (CSV), JavaScript Object Notation (JSON), electronic data sheet (EDS), and extensible markup language (XML) telemetric and command exchange (XTCE) formats.  Data can be exported from the application to files in CSV, JSON, EDS, and XTCE formats.  The CCDD tables also allow simple cut and paste operations from the host operating system’s clipboard.  To make use of the project’s data, CCDD can interact with Java Virtual Machine (JVM)-based scripting languages via a set of supplied data access methods.  Using scripts, the user can translate the data stored in the CCDD’s database into output files.  Example scripts for creating common CFS related output files are provided in four of these scripting languages.  An embedded web server can be activated, allowing web-based application access to the data.

See the CCDD installation guide for details on set up.  See the developer's and user's guides for details on use.  A tutorial is also provided.

_Note: The master branch contains_ **CCDD version 1**_, which is superseded by_ **CCDD version 2** _located in branch CCDD-2;_ **CCDD version 1** _is no longer being updated_

## CCDD version 2 (CCDD-2 branch)

* Beginning with CCDD version 2.1 PostgreSQL versions 12 and later are supported

* CCDD version 2 works with Java 7-13

*** Version 2.1.1 has been released **

Below is a brief description of what has changed in version 2.1.1
* The CCDD 1 -> CCDD 2 conversion patch is removed. Github now shows CCDD 2 as the default branch. CCDD 1 (the master branch) is retained for archival purposes
* Added SQL network (database response) and query timeouts. The default is 5 seconds; the value can be altered in the Preferences dialog
* Updated the embedded JDBC driver to 42.5.2 (supports Java 8 and above)
* Added missing database roll-back commands if an error occurs making a verification fix or when modifying tables due to input type changes
* Added save point and roll-back commands when creating, renaming, copying, and modifying tables
* Corrected SQL error when copying the currently open project
* Changed the rename project command to allow renaming the currently open project
* Corrected an out of index error if pasting into a non-data table (e.g., macro editor) and insufficient rows exist for the pasted data
* Changed the behavior when pasting structure table data. Pasted data is handled as with other tables where each cell is evaluated just as if the user is typing the cell value in one at a time in sequence (left to right, top to bottom). If the structure contains an array and the array members are hidden then when pasting data the array member rows are treated as if they aren't there (i.e., the array member rows are skipped and pasting continues on the next row). If the array members are visible (expanded) then the pasted data does not skip the array member cells, but treats them like all other cells
* Deleted a duplicate call to update open table editors if a table type is altered
* Added an update to the fixed column's row sorter when a data table is displaying the fixed column
* Removed call to fireTableDataChanged in loadDataArrayIntoTable to prevent the table from scrolling unexpectedly after inserting a row, then altering a cell in the new row
* Removed call to addChangeListener in FixedColumnHandler to allow scrolling to the bottom of a table when a row is inserted
* Changed getTableTreePathList to use a HashSet so that duplicate variables are automatically excluded
* Simplified the verification of the custom values table
* Corrected JSON and CSV export bug introduced in version 2.1.0
* Changed table row insertion so that the row is inserted at the row selected (instead of below it)
* Corrected error when performing table deletions where changes were not committed to the database
* Corrected bug when deleting custom value entries for tables with square brackets ([ or ]) in the name (i.e., structure arrays). This led to duplicate entries for a table's columns in the custom values table
* Corrected bug when importing a CSV or JSON file into an open table editor
* Adjusted error handling in the CSV handler
* Corrected formatting of the snapshot2 JSON data output to prevent false file difference indications
* Increased the default values for "PostgreSQL connection timeout" (was 5 seconds, now 10) and "PostgreSQL database timeout" (was 5 seconds, now 60), and increased the maximum values from 60 to 600 seconds. Queries for large databases can take longer then the old default value of 5 seconds ("PostgreSQL database timeout")

*** Version 2.1.0 has been released **

Below is a brief description of what has changed in version 2.1.0
* Where applicable, assign the 'owner' dialog when performing a background operation so that the dialog's controls are disabled
* Simplified pre-loading table members and default table tree creation handling (pre-loading and the default tree can speed opening dialogs containing a tree and filtering the tree)
* Added pre-building of a table type filtered default tree
* Where applicable, moved background completion steps to the execution method so that the GUI is released when the pointer indicates the operation is complete
* Updated the SQL function for loading table members so that array members are sorted. This prevents needing to perform the sort in the Java code, which is much slower
* Broke up lengthy SQL commands into multiple, shorter commands (a continuation of updates made in version 2.0.32)
* Added a check in the database verification for duplicate custom values entries (entries having the same table path and column; there should never be more than one)
* Changed the database verification dialog so that it remains visible after an issue (or issues) is fixed so that other, unselected issues can be selected to fix without having to rerun the verification
* Updated the data table editor to prevent unexpected scrolling when one or more table rows is added/deleted
* Eliminated the use of the built-in OID (object identifiers) support. Built-in OID support was removed beginning with PostgreSQL 12.  This update allows the use of PostgreSQL 8 and above with CCDD. In place of the built-in OID column support are added to the internal database tables that behave in the same manner as the built-in columns.  A database created using an earlier version can be modified to this version when the project is opened (_the conversion is not reversible_)
* Corrected parsing of data values when pasting into a table from the clipboard. Tab characters (\t) were inadvertently changed to spaces in an earlier version. Also corrected handling of pasting data that extends outside a non-data table's column boundaries (excess data is simply ignored)
* Updated the C header to structure conversion and import. More complex macros are allowed as array sizes

*** Version 2.0.38 has been released **

Below is a brief description of what has changed in version 2.0.38
* Corrected the exception thrown due to a missing ENUM table type when opening a database for the first time
* Removed patches from the patch handler dated before 6/1/2019. These patches can be found in version 2.0.37

*** Version 2.0.37 has been released **

Below is a brief description of what has changed in version 2.0.37
* Adding a new column to a table type could result in an error; this has been corrected
* The utility method to remove HTML tags no longer attempts remove tags type unused in CCDD. Removal of 'generic' tags could, in certain cases, eliminate portions of the string that were not actual tags

*** Version 2.0.36 has been released **

Below is a brief description of what has changed in version 2.0.36
* A bug that affected validating the contents of data pasted into table cells is corrected
* When exporting tables in JSON format if a NULL table cell value is found it's converted to a blank

*** Version 2.0.35 has been released **

Below is a brief description of what has changed in version 2.0.35
* The Import dialog is separated into five separate dialogs based on import file type (CSV, EDS, JSON, XTEC, and C Header). Check boxes that don't apply to the import type are removed from that type's dialog
* Corrected unintentional table scrolling when cell editing is completed, or when (re)moving one or more rows
* Combined the Show Variables and Search Variables dialogs
* EDS and XTCE import/export functionality is restored. Note that the limitations of the EDS and XTCE formats still exist, so only basic table data can be exported/imported
* Changed internal table storage to use the 'TRUNCATE' rather than the 'DROP TABLE' command. The 'DROP' creates a lock in the database that remains until the changes are committed; for a large number of table updates these locks can use up all of the shared memory and cause a SQL command failure
* During the table modification process the data fields are checked to see if any have changed; the internal fields table is only rewritten if a change occurred
* The method that extracts an enumeration group separator now works for enumerations with more than one enumeration value (example: 0|First group first value|First group second value,1|Second group first value|Second group second value)
* Corrected export path storage bug that erroneously stored the file name with the path
* The User's Guide is updated to reflect the latest changes

*** Version 2.0.34 has been released **

Below is a brief description of what has changed in version 2.0.34
* The Table Type Editor dialog converts a user-defined column name to one that meets the PostgreSQL constraints when creating the table in the database. The constraints are that the column name contains only lowercase letters, numerals, or underscores, and only begins with a letter or underscore. The conversion replaces any 'illegal' characters with an underscore (example: A@b.com is converted to a\_b\_com). CCDD erroneously allowed a leading numeral in the database version of the column name, causing table access errors. A column name with a leading numeral now has an underscore prepended (example: 1\_is\_the\_name is converted to \_1\_is\_the\_name)
* A scale factor input is added to the Preferences dialog Fonts tab. The scale factor is applied to all font sizes. Fractional values are allowed. Scaling is useful when using high definition monitors in operating systems (e.g., certain Linux versions) that don't apply font scaling to all applications
* A fixed, or 'frozen' column can be enabled in the Table Editor. The first column displayed is duplicated and displayed to the left of the table. If the table is scrolled horizontally the fixed column remains in position. Dragging another column to the first position changes the fixed column to the duplicate this new column
* Restored the Show Variables dialog

*** Version 2.0.33 has been released **

Below is a brief description of what has changed in version 2.0.33
* More improvements in speed have been made in handling SQL commands for table updates. This can significantly improve the time it takes to complete an update for large tables

*** Version 2.0.32 has been released **

Below is a brief description of what has changed in version 2.0.32
* Improvements in speed have been made in handling SQL commands for table updates. This can significantly improve the time it takes to complete an update for large tables
* Maximum SQL command length is based on a user-specified (Preferences) value

*** Version 2.0.31 has been released **

Below is a brief description of what has changed in version 2.0.31
* Corrected a bug in the CSV data type export (types were not sorted)
* Corrected a file/path error when restoring a database from multiple JSON or CSV files

*** Version 2.0.30 has been released **

Below is a brief description of what has changed in version 2.0.30
* Corrected issues with JSON and CSV file import and export
* Corrected storage of last directory accessed

*** Version 2.0.25 has been released **

Below is a brief description of what has changed from version 2.0.24 to 2.0.25.
* Addressed a bug that prevented CCDD from auto-correcting data field related issues found during the verification process
* Addressed a bug that prevented users from importing JSON or CSV files that contained info for tables without any rows of data
* Addressed a bug that prevented group data from being exported in CSV format when any of the tables were array members
* Refactored the assign-message-id dialog. Users can now use a table tree to select/omit tables when assigning message ids. Details can be found in the users guide.
* Broke the users guide up to make it more approachable and moved all documents into the 'Docs' directory.
* Updated the CCDD tutorial
* Refactored the code that imports an entire database from a single JSON/CSV file to be more efficient
* Introduced new data access methods in an attempt to make data easier to access in the future while retaining all current data access methods to prevent breaking any existing   scripts that projects may be using.
* Addressed a bug that prevented a new root and a child of the new root from being imported at the same time
* Refactored the code that imports data fields as users were encountering many issues when importing various data field changes.
* Refactored the code that clears a directory prior to an export so that it can no longer delete folders. It can only delete JSON or CSV files now. * This is to prevent accidental deletion of unrelated files if an improper directory is selected for an export
* Addressed a performance issue that caused some scripts to take an abnormally long time to finish
* The external C_Header_To_CSV script is no longer needed when attempting to import C header files into CCDD. A user can now directly import them into CCDD by selecting the C_Header import option within CCDD. More details can be found in the users guide.
* Addressed performance issues that were causing the script manager to take an abnormally long time to launch when a large CCDD database was open.
* Addressed performance issues that were causing the 'Filter By Group' option to take an abnormally long time to rebuild the table tree for large databases.
* Addressed an issue that prevented group data fields from being imported via JSON or CSV
* Merged the 'Show variables' and 'Search variables' dialogs
* Addressed a bug that was preventing the table type data from being exported unless the entire database was being exported

*** Version 2.0.24 has been released **

* CCDD has 2 new data types which can be assigned in the data type manager which are 'Structure' and 'Enum'. The Structure data type was added so that users do not have to define a table for every 'Structure' based data type they wish to use. So if you have a Structure that is not defined within your database, like CFE structures that are often shared, but you wish to assign the type to a variable you can now add that type to the data type manager without creating a table and defining all the elements that the structure consists of. The same goes for the 'Enum' type but this can be used for 'typedef enum' structures. 
* A new table type was added to CCDD called 'Enum'. This new table allows users to store the names of all of the enums that make up a 'typedef enum' structure. Once this table is defined users can assign the data type to any variable they wish.
* Two new data access methods were added for the new 'Enum' table type so that a user can retrieve all defined Enum tables and their members via scripts. More details available in the users guide.
* The copy and paste functionality has been fixed. 

*** Version 2.0.23 has been released **

Below is a brief description of what has changed from version 2.0.22 to 2.0.23.
* CCDD can now import and verify 3d arrays
* CCDD can now 'replace existing associations' during an import.
* Fixed a few issues related to changing the size of a macro during an import. Could previously cause errors if the macro was used to define the size of an array.
* Fixed a few issues related to data fields being duplicated for many tables after an import.
* Added a new option to the export command that is used within scripts. For the 'tablepaths' sub-command you can now specify 'all' to export ALL tables within a database.
* Fixed a few issues that prevented importing xtce files.
* Fixed a few issues that were causing array members not to update during an import if they were a member of a non-root prototype table.
* Added new functionality to CCDD so that a user can now import new table type or changes to an existing table type if they wish. All associated files will be updated during the import. 
* Updated the users guide to include information related to the new 'tablepaths' option and the new 'replaceExistingAssociations' flags.

*** Version 2.0.22 has been released **

Below is a brief description of what has changed from version 2.0.21 to 2.0.22.
* CCDD can now detect and better report issues related to unexpected JSON formats during an import
* Addressed an issue related to the primary key of tables not being given the correct value which resulted in many databases failing the verification process.
* Addressed an issue that prevented the internal groups table from being updated during an import.
* Addressed an issue that was preventing users from creating new tables.
* Addressed an issue where macro changes that removed tables which were included in an active group were not updating the internal groups table.
* Duplicate macros will now be filtered druing a JSON or CSV import
* Addressed an issue where a user would end up with duplicated data if they imported a change to a member of a non-primitive array.
* Multiple performance optimizations that greatly increase the speed at which a database can be opened or verifived and the speed at which CCDD can process a change in very large macros.
* Addressed an issue where some tables could not be directly opened from the seach dialog.
* Addressed an issue where some databases could not be converted from v1 to v2.

*** Version 2.0.21 has been released **
The ccdd.build.xml file is included in the repo. I have added notes to section 4.1 of the users guide that describe how to rebuild the tool if needed.

Below is a brief description of what has changed from version 2.0.20 to 2.0.21.
* Users were getting numerous errors, including a JDBC error, due to a change to the build.xml file that did not work as expected. This has been corrected.
* The replace existing tables check box during import did not replace the table, but simply appended to it.
* When exporting a single table, via the table editor, an empty table_info.json file was created.
* The use existing fields checkbox would create empty rows for each duplicate row.
* When exporting a table from the table editor it did not append the file name as it used to which forced the user to type out the name of the file each time.
* When exporting a table from the table editor window most of the options are now disabled. Example, Export entire database makes no sense when only one table is being exported.
* When an export failed CCDD sometimes reported that it passed.
* When importing directly into a table that is open, via the table editor, all data fields were cleared.
* When exporting a table from the table editor, via JSON, the end of the file was missing a parenthesis and had a comma added.
* When importing data any booleans would be set to false regardless of their actual value
* When importing data any duplicate rows would cause empty rows to be added to the end of the table
* When importing data fields the currently open table would often look correct, but once the user attempted to store the data it would cause an error and fail
* XTCE and EDS files can not store data field info. When importing one of these two file types all data field info was lost if the user elected to overwrite or append new data. Added a new checkbox that allows the user to chose if they wish to keep all currently existing data fields.
* Arrays could not be properly imported and had numerous issues
* When importing into a table of type "Command" the "Command Argument" column was always set to false regardless of its value which caused errors as the column is not of type boolean.
* Fixed an error that was preventing 2d arrays from being imported
* Fixed an error that occurred when macros were used to define the size of an array.
* Fixed an issue where the patch handler was sometimes requesting that a database be patched even if it was already a v2 database. This only occurred when launching from the command line.
* When backing up a database the name of the database will now be the same as the selected file name.
* COPY AND PASTE FUNCTIONALITY IS NOT WORKING AS EXPECTED. IT CAN BE USED, BUT DOES NOT WORK AS WELL AS IT DID IN THE PAST. WILL BE ADDRESSED IN A FUTURE RELEASE.

*** Version 2.0.20 has been released **
* Fixed an issue with the patch handler that caused it to fail to convert a database from version 1 to version 2 if any text fields within the database contained an apostrophe.
* Fixed an issue with the patch handler where the internal __orders table was not being updated when converting a v1 database to v2. This resulted in the database failing when the "verify" functionality was used and reporting that tables with table type "Command" had out of order columns.
* Fixed an issue with the patch handler related to the conversion of tables with table type "Command". If these tables contained an array they were not being properly expanded. This resulted in the database failing to "verify" due to missing table members.
* Fixed an issue with v2 where data fields for certain tables were being cleared. If a v2 database was opened and then closed all of the data fields for tables with table type "Command" were being deleted.
* By default the "Command" event filter checkbox is unchecked in order to increase performance.
* Added a new checkbox to the export dialog that allows the user to decide if they would like to clear the contents of the target directory before an export.
* psql 8.4 - 11 is now supported. You can now create a .dbu backup on a machine with psql 11 and restore it on a machine with psql 8.4 or vice versa.
* Restored EDS and XTCE import/export functionality to CCDDv2. Works just as it did in v1.
* CSV import/export functionality has been updated to match the new JSON import/export functionality. You can now export entire databases, including internal tables, to JSON or CSV files. The data can be placed in separate files, every table in the database will get its own file, or one large file. This is useful for tracking changes to the database as these files can be easily checked into git. This also allows the user to make small changes to these files then perform an import which will update the database as needed.
* When files are compared for differences during an import and EOL characters will not be ignored.

## CCDD version 1 (master branch)

_Note:_ **CCDD version 1** _is no longer being updated; use _ **CCDD version 2**

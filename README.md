PunParse
========
PunParse parses data from PunBB HTML output into a database that
somewhat resembles a PunBB 1.2 database. Under development.

Requirements
------------
You need Java 7 or newer to run PunParse. To do anything useful,
you also need an SQL database. Currently, only MySQL is supported.
Support for PostgreSQL and SQLite might be added in the future.

Arguments
---------
PunParse requires three command line arguments, in this order:
* The folder containing HTML to parse. Irrelevant files are ignored.
* The database URL to write to. A URL might look like this:
  <pre>mysql://localhost/?user=username&password=password</pre>
* The database name to write to. The database should be empty,
  unless you are trying to append data to an existing PunBB database.
  Existing records will not be modified.

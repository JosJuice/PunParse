PunParse
========
PunParse parses data from PunBB HTML output into a database that
somewhat resembles a PunBB 1.2 database. Under development.

Arguments
---------
PunParse requires three command line arguments.
* The folder containing HTML to parse. Irrelevant files are ignored.
* The database URL to write to. A URL might look like this:
  <pre>mysql://localhost/?user=username&password=password</pre>
* The database name to write to. The database should be empty,
  unless you are trying to append data to an existing PunBB database.

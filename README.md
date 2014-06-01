PunParse
========
PunParse parses data from PunBB HTML output into a database that
somewhat resembles a PunBB 1.2 database. Under development.

Requirements
------------
You need Java 7 or newer to run PunParse. You also need a connection to
an SQL database. Currently, only MySQL is supported. Support for
PostgreSQL (9.1 and newer) and SQLite is under development. However, PostgreSQL has not
been tested, and there is currently a problem; normally, nothing happens
when PunParse tries to create a record with a primary key that already
exists, but with PostgreSQL, an error will occur. One case where this
will happen is if one piece of data (for instance, a post) is present in
two HTML files.

Arguments
---------
PunParse requires three command line arguments, in this order:
* The folder containing HTML to parse. Irrelevant files are ignored.
* The database URL to write to. A URL might look like
  <pre>mysql://localhost/?user=username&password=password</pre> or
  <pre>postgresql://localhost/?user=username&password=password</pre> or
  <pre>sqlite:database.db</pre>
* The name of the database to write to. The database should be empty
  unless you are trying to append data to an existing PunBB database.
  Existing records will not be modified.

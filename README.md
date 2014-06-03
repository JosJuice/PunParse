PunParse
========
PunParse parses data from PunBB HTML output into a database that
somewhat resembles a PunBB 1.2 database. Under development.

Requirements
------------
You need Java 7 or newer to run PunParse. You also need a connection to
an SQL database. Currently, only MySQL is supported. Support for
PostgreSQL and SQLite is under development. However, PostgreSQL has not
been tested, and there is currently a problem; normally, nothing happens
when PunParse tries to create a record with a primary key that already
ixists, but with PostgreSQL, an error will occur. One case where this
happens is when one record (for instance, a post) is present in two HTML
files.

Arguments
---------
PunParse requires two command line arguments, in this order:
* The folder containing HTML to parse. Irrelevant files are ignored.
* The URL of the database to write to. A URL might look like one these:
  * `mysql://localhost/database?user=username&password=password`
  * `postgresql://localhost/database?user=username&password=password`
  * `sqlite:database.db`

There are also optional command line arguments:
* `--append` makes PunParse skip creating tables and indexes. This is
  useful if you want to append data to an existing database.

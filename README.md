# Lojdouts

Are you tired of needing to individually remember/lookup the three different 
libraries (and their versions!) you need when you want to sanely interface with
a MySQL database in Clojure?

What if you could have a collections of maps, each one defining a dedicated 
section of your deps/bb.edn file?

Where instead of running
```bash
neil dep add com.github.seancorfield/honeysql
neil dep add com.github.seancorfield/next.jdbc
neil dep add mysql/mysql-connector-java
```

You could just run
```bash
lojd add mysql
```

Enter Lojdouts, a babashka script for managing your deps-style projects'
loadouts.

## Installation

(Requires babashka)

1. Clone the lojdouts repo `https://github.com/samuelludwig/lojdouts.git`.
2. Run the build task via `bb build:clean`.
3. Copy the shebanged, executable script to wherever you like to put your bins
via `cp ./target/lojd <my-bin-directory>`.

## Usage

FIXME: explanation

- `cp ./doc/example-configs/loadouts.edn ~/.config/lojdouts/loadouts.edn`
- `lojd {list, view, add} [loadout-name] [--bb]`

## Options

FIXME: listing of options this app accepts.

## Examples

...

### Bugs

...

### Any Other Sections
### That You Think
### Might be Useful

## License

Copyright © 2023 Dot

_EPLv1.0 is just the default for projects generated by `deps-new`: you are not_
_required to open source this project, nor are you required to use EPLv1.0!_
_Feel free to remove or change the `LICENSE` file and remove or update this_
_section of the `README.md` file!_

Distributed under the Eclipse Public License version 1.0.

# biograkn-download

Shell script to download the data sources required by biograkn.

It may not work as is, because it depends by the location of the following standard tools:

`wget` `gzip` `grep` `awk` `uniq` `cut` `tail` `sort` `sed`

It also depends by `xlsx2csv`, an xlsx to csv converter written in python (see <http://github.com/dilshod/xlsx2csv>).

Type 

```
sudo easy_install xlsx2csv
```

or

```
pip install xlsx2csv
```

in a terminal window to install.
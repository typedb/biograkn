#!/bin/bash

STARTDIR=`pwd`

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

WGET=/opt/local/bin/wget
GZIP=/usr/bin/gzip
RSYNC=/usr/bin/rsync
GREP=/usr/bin/grep
AWK=/usr/bin/awk
UNIQ=/usr/bin/uniq
CUT=/usr/bin/cut
TAIL=/usr/bin/tail
SORT=/usr/bin/sort
SED=/usr/bin/sed

# xlsx to csv converter (http://github.com/dilshod/xlsx2csv)
# sudo easy_install xlsx2csv / pip install xlsx2csv
XLSX2CSV=/usr/local/bin/xlsx2csv

TODAY=$(date +%Y-%m-%d)

cd $DIR/datasources

$WGET -N -i $DIR/urls.lst -o $DIR/log/status_$TODAY.log

if [[ $RC -ne 0 ]]; then
        echo 'failed with returncode:' $RC > $DIR/log/error_$TODAY.log
        exit $RC
fi

$GZIP -dfk *.gz

$GREP '^9606' gene2go | $AWK '{ print $2, $3; }' | $UNIQ > gene2go_human
$GREP -v '^R.' miRNA.dat > miRNA.txt
$GREP R-HSA gene_association.reactome | $AWK '{ print $4 "\t" substr($5,index($5,":")+1) }' | $UNIQ > pathway2go.txt
LCTYPE=C LANG=C $CUT -f 1,2,3 -s miRCancerOctober2017.txt | $TAIL -n +2 | $SORT | $UNIQ > miRCancers.txt
$SED $'s/<\/entry>/&\\\n/g' uniprot_sprot.xml > uniprot_sprot2.xml
$GREP 'R-HSA-' Uniprot2Reactome.txt | $AWK '{ print $1, $2; }' | $UNIQ > UniProt2Reactome_human
$XLSX2CSV -d 'tab' hsa_MTI.xlsx hsa_MTI.txt

cd $STARTDIR
# BioGrakn

[![CircleCI](https://circleci.com/gh/graknlabs/biograkn/tree/master.svg?style=shield)](https://circleci.com/gh/graknlabs/biograkn/tree/master)
[![Slack Status](http://grakn-slackin.herokuapp.com/badge.svg)](https://grakn.ai/slack)
[![Discussion Forum](https://img.shields.io/discourse/https/discuss.grakn.ai/topics.svg)](https://discuss.grakn.ai)
[![Stack Overflow](https://img.shields.io/badge/stackoverflow-grakn-796de3.svg)](https://stackoverflow.com/questions/tagged/grakn)
[![Stack Overflow](https://img.shields.io/badge/stackoverflow-graql-3dce8c.svg)](https://stackoverflow.com/questions/tagged/graql)


# BioGrakn

BioGrakn is a collection of knowledge graphs of biomedical data demonstrating the following use-cases:

| Use Case | keyspace name | Datasets
|:------------|:--------------|:--------------|
| 1. Precision Medicine | precision_medicine | [ClinicalTrials.gov](https://clinicaltrials.gov/ct2/home), [ClinVar](https://www.ncbi.nlm.nih.gov/clinvar/), [CTDBase](http://ctdbase.org/), [DisGeNet](http://www.disgenet.org/), [Drugs@FDA](https://www.accessdata.fda.gov/scripts/cder/daf/index.cfm), [HGNC](https://www.genenames.org/), and [PharmGKB](https://www.pharmgkb.org/) |
| 2. Text Mining | text_mining | [PubMed](https://www.ncbi.nlm.nih.gov/pubmed/) |
| 3. BLAST | blast | N/A
| 4. Disease Network | disease_network | [Uniprot](https://www.uniprot.org/), [Reactome](https://reactome.org/), [DGIdb](http://www.dgidb.org/), [DisGeNET](http://www.disgenet.org/web/DisGeNET/menu;jsessionid=np5qutaldora6gql80xqhmen), [HPA-Tissue](https://www.proteinatlas.org/humanproteome/tissue+specific), [EBI IntAct](https://www.ebi.ac.uk/intact/), [Kaneko](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC3558318/), [Gene Expression Omnibus](https://www.ncbi.nlm.nih.gov/geo/) and [TissueNet](http://netbio.bgu.ac.il/tissuenet/) |


BioGrakn provides an intuitive way to query interconnected and heterogeneous biomedical data in one single place. The schema that models the underlying knowledge graph alongside the descriptive query language, Graql, makes writing complex queries an extremely straightforward and intuitive process. Furthermore, the automated reasoning capability of Grakn, allows BioGrakn to become an intelligent database of biomedical data that infers implicit knowledge based on the explicitly stored data. BioGrakn can understand biological facts, infer based on new findings and enforce research constraints, all at query (run) time.

## Quickstart

1. [Download BioGrakn](https://storage.googleapis.com/biograkn/grakn-core-1.5.7-biograkn-0.2.zip)
2. Unzip the downloaded file.
3. `cd` into the unzipped folder, via terminal or command prompt.
4. run `./grakn server start`
5. [Download Grakn Workbase 1.2.2](https://github.com/graknlabs/workbase/releases/tag/1.2.2) (note that, at the moment, newer versions of Grakn Workbase are not yet compatible with BioGrakn)

## Interacting With BioGrakn
Queries can be run over BioGrakn, via Graql Console, Grakn Clients and Grakn Workbase.

### Via Graql Console
While inside the unzipped folder, via terminal or command prompt, run: `./grakn console -k keyspace_name`. The console is now ready to answer your queries.

### Via Grakn Clients
Grakn Clients are available for [Java](https://github.com/graknlabs/client-java), [Node.js](https://github.com/graknlabs/client-nodejs) and [Python](https://github.com/graknlabs/client-python). Using these clients, you will be able to perform read and write operations over BioGrakn.

See an example of how this is done in the [Grakn <> BLAST integration example](./blast/queries.py), using the [Python](https://github.com/graknlabs/client-python) client.

### Via Grakn Workbase
Download the [latest release of Grakn Workbase](https://github.com/graknlabs/workbase/releases), install and run it.

Read the [documentation on Workbase](https://dev.grakn.ai/docs/workbase/overview) or watch a short series of videos about [using workbase with the Grakn <> BLAST integration example](https://www.youtube.com/watch?v=pHIer5roF4c&list=PLtEF8_xCPklaTR4RaB3ng9V3Ov7n980cQ).

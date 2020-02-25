# Disease Network

BioGrakn DN is a single knowledge graph of biomedical data describing a disease network, ingested from [Uniprot](https://www.uniprot.org/), [Reactome](https://reactome.org/), [DGIdb](http://www.dgidb.org/), [DisGeNET](http://www.disgenet.org/web/DisGeNET/menu;jsessionid=np5qutaldora6gql80xqhmen), [HPA-Tissue](https://www.proteinatlas.org/humanproteome/tissue+specific), [EBI IntAct](https://www.ebi.ac.uk/intact/), [Kaneko](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC3558318/), [Gene Expression Omnibus](https://www.ncbi.nlm.nih.gov/geo/) and [TissueNet](http://netbio.bgu.ac.il/tissuenet/).


## Example Queries

### Which protein(s) are encoded by the gene with entrez-id of 100137049?

```
match
  $gpe (encoding-gene: $ge, encoded-protein: $pr) isa gene-protein-encoding;
  $ge isa gene has entrez-id "100137049";
limit 10; get;
```

![Proteins encoded by gene with entrez-id of 100137049](./query-examples/q-1.png)

### Which diseases affect the appendix tissue?
Note that the data to answer this question is not explicitly stored in the knowledge graph. The [`protein-disease-association-and-tissue-enhancement-implies-disease-tissue-association Rule`](./schema.gql#L216) enables us to get the answer to this question using the following query.

```
match
  $ti isa tissue has tissue-name "appendix";
  $dta (associated-disease: $di, associated-tissue: $ti) isa disease-tissue-association;
limit 10; get;
```

![Disease that affect appendix tissue](./query-examples/q-2.png)

### What are the proteins associated with Asthma?
Note that the data to answer this question is not explicitly stored in the knowledge graph. The [`gene-disease-association-and-gene-protein-encoding-protein-disease-association Rule`](./schema.gql#L169) enables us to get the answer to this question using the following query.

```
match
  $di isa disease has disease-name "Asthma";
  $dda (associated-protein: $pr, associated-disease: $di) isa protein-disease-association;
limit 10; get;
```

![Proteins associated with Asthma](./query-examples/q-3.png)


### Which diseases are associated with protein interactions taking place in the liver?
This query also makes use of the [`gene-disease-association-and-gene-protein-encoding-protein-disease-association Rule`](./schema.gql#L169).

```
match
  $ti isa tissue, has tissue-name "liver";
  $pr isa protein;
  $pr2 isa protein;
  $pr != $pr2;
  $di isa disease;
  $pl (tissue-context: $ti, biomolecular-process: $ppi) isa process-localisation;
  $ppi (interacting-protein: $pr, interacting-protein: $pr2) isa protein-protein-interaction;
  $pda (associated-protein: $pr, associated-disease: $di) isa protein-disease-association;
limit 30; get;
```

![Diseases associated to protein interactions taking place in liver](./query-examples/q-4.png)


### Which drugs and diseases are associated with the same differentially expressed gene from comparisons made in geo-series with id of GSE27876?

```
match
  $geo-se isa geo-series has GEOStudy-id "GSE27876";
  $comp (compared-groups: $geo-comp, containing-study: $geo-se) isa comparison;
  $def (conducted-analysis: $geo-comp, differentially-expressed-gene: $ge) isa differentially-expressed-finding;
  $dgi (target-gene: $ge, interacted-drug: $dr) isa drug-gene-interaction;
  $gda (associated-gene: $ge, associated-disease: $di) isa gene-disease-association;
limit 10; get;
```

![Diseases and drugs associated with differentially expressed gene from comparisons made in geo-series with id of GSE27876](./query-examples/q-5.png)


## References
- **[BioGrakn DN: Accelerating Biomedical Knowledge Discovery with a Grakn Knowledge Graph](https://blog.grakn.ai/BioGrakn-accelerating-biomedical-knowledge-discovery-with-a-grakn-knowledge-graph-84706768d7d4)**
- **[BioGrakn DN: A Knowledge Graph-Based Semantic Database for Biomedical Sciences](https://link.springer.com/chapter/10.1007/978-3-319-61566-0_28)**

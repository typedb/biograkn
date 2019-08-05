from grakn.client import GraknClient
import csv 
import os

client = GraknClient(uri="localhost:48555")
session = client.session(keyspace="disease_network")

# Parameters to set how much data to load from each dataset
nUni = 10000000
nInt = 10000000
nRea = 10000000
nDis = 10000000
nHPA = 10000000
nKan = 10000000
nGEO = 10000000
nDGI = 10000000
nTis = 10000000
sim = 10000000

# -------
# 1. START UniProt

# Uniprot
if nUni is not 0:
	print('1. Uniprot')
	with open('../dataset/uniprot/uniprot.csv', 'rt', encoding='utf-8') as csvfile:
		csvreader = csv.reader(csvfile, delimiter='	')
		raw_file = []
		n = 0
		for row in csvreader: 
			n = n + 1
			if n is not 1:
				raw_file.append(row)

	uniprotdb = []
	for i in raw_file[:nUni]:
		data = {}
		data['uniprot-id'] = i[0]
		data['protein-name'] = i[3]
		uniprotdb.append(data)

	# Insert proteins
	tx = session.transaction().write()
	counter = 0
	for q in uniprotdb: 
		counter = counter + 1
		graql = 'insert $a isa protein, has uniprot-id "' + q['uniprot-id'] +'", has uniprot-name "' + q['protein-name'] +'";'
		print(graql)
		tx.query(graql)
		if counter % 100 == 0:
			tx.commit()
			print('committed!')
			tx = session.transaction().write()
			print(counter)
	print('committed!')
	tx.commit()

# END Uniprot
# -------


# ----------
# 2. IntAct PPIs

print('2. IntAct PPIs')

if nInt is not 0:
	with open('../dataset/intact/PPIs.csv', 'rt', encoding='utf-8') as csvfile:
		csvreader = csv.reader(csvfile, delimiter='	')
		raw_file = []
		n = 0
		for row in csvreader: 
			n = n + 1
			if n is not 1 and nInt is not 0:
				raw_file.append(row)

	ppi = []
	for i in raw_file[:nInt]:
		data = {}
		data['protein-a'] = i[2][10:]
		data['protein-b'] = i[3][10:]
		ppi.append(data)

	tx = session.transaction().write()
	counter = 0
	for p in ppi:
		counter = counter + 1
		graql = 'match $a isa protein, has uniprot-id "' + p['protein-a'].replace('"', '') + '"; $b isa protein, has uniprot-id "' + p['protein-b'].replace('"', '') + '"; insert (interacting-protein: $a, interacting-protein: $b) isa protein-protein-interaction;'
		tx.query(graql)
		print('execute ' + graql)
		if counter % 100 == 0:
			tx.commit()
			print('committed!')
			tx = session.transaction().write()
			print(counter)

	tx.commit()
	print('committed!')

# END IntAct PPIs
# ----------



# ----------
# 3. Reactome Pathways 

print('3. Reactome')

if nRea is not 0:
	with open('../dataset/reactome/UniProt2Reactome_All_Levels.txt', 'rt', encoding='utf-8') as csvfile:
		csvreader = csv.reader(csvfile, delimiter='	')
		raw_file = []
		n = 0
		for row in csvreader: 
			n = n + 1
			if n is not 1:
				raw_file.append(row)

	pathways = []
	pathways_ids = []
	print('Parsing raw file...')
	for i in raw_file[:nRea]:
		if i[5] == "Homo sapiens":
			data = {}
			data['uniprot-id'] = i[0]
			data['pathway-id'] = i[1]
			data['pathway-name'] = i[3]
			pathways.append(data)
			pathways_ids.append(i[1])

	tx = session.transaction().write()
	counter = 0;
	list_of_pathways = []
	pathways_ids = (list(set(pathways_ids)))

	# Preprocessing pathways + proteins. 
	for p in pathways_ids:
		counter = counter + 1 
		print('Preprocessing: ' + str(counter) + '/' + str(len(pathways_ids)) + ' pathways.')
		new_d = {}
		new_d = {'pathway-id': p, 'proteins': []}
		for pi in pathways:
			if pi['pathway-id'] == p:
				new_d['proteins'].append(pi['uniprot-id'])
		list_of_pathways.append(new_d)

	counter = 0
	for l in list_of_pathways: 
		counter_pr = 0
		counter = counter + 1
		# Insert Pathways
		q = 'insert $path isa pathway, has pathway-id "' + l['pathway-id'] + '";'
		tx.query(q)
		tx.commit()
		tx = session.transaction().write()
		pr_counter = 0
		# Insert pathway-participation relationship between protein + pathway
		for p in l['proteins']:
			pr_counter = pr_counter + 1
			print('Inserting ' + str(counter) + '/' + str(len(list_of_pathways)) + ' pathways & ' + str(pr_counter) + '/' + str(len(l['proteins'])) + ' proteins.' )
			counter_pr = counter_pr + 1
			q = "match $pr isa protein, has uniprot-id '" + p + "'; $path isa pathway, has pathway-id '" + l['pathway-id'] + "'; insert (participating-protein: $pr, participated-pathway: $path) isa pathway-participation;"
			tx.query(q)
			if pr_counter % len(l['proteins']) == 0:
				tx.commit()
				tx = session.transaction().write()
				print('commit.')
	tx.commit()

# END Reactome Pathways 
# ----------

# ----------
# 4. START DisGeNET

print('4. DisGeNET')
if nDis is not 0:
	with open('../dataset/disgenet/curated_gene_disease_associations.csv', 'rt', encoding='utf-8') as csvfile:
		csvreader = csv.reader(csvfile, delimiter='	')
		raw_file = []
		n = 0
		q = ''

		counter = 0

		for row in csvreader: 
			counter = counter + 1
			if counter is not 1 and counter < nDis:
				print('Preprocessing disease data: ' + str(counter))
				d = {}

				row = row[0].split(',')

				d['geneId'] = row[0]
				d['gene-symbol'] = row[1]
				d['diseaseId'] = row[2]
				d['disease-name'] = row[3]
				raw_file.append(d)
				q = q + '	' + row[0]

	# Commit entrez-id after matching with uniprot-ids
	import urllib
	import urllib.request

	url = 'https://www.uniprot.org/uploadlists/'

	params = {
	'from':'P_ENTREZGENEID',
	'to':'ACC',
	'format':'tab',
	'query': q
	}

	import ssl
	print('Sending to uniprot API')
	context = ssl._create_unverified_context()
	data = urllib.parse.urlencode(params).encode("utf-8")
	request = urllib.request.Request(url, data)
	contact = "faegs@gmail.com" 
	request.add_header('User-Agent', 'Python %s' % contact)
	response = urllib.request.urlopen(request, context=context)
	page = response.read(20000000).strip()
	page = page.split(b'\n')[1:]
	page = [s.split(b'\t') for s in page]

	print(len(page))

	# Commit ENTREZ 
	tx = session.transaction().write()
	print('4. DisGeNET (Committing ENTREZ)')
	counter = 0
	for i in page:
		counter = counter + 1
		uniprotid = i[1].decode("utf-8")
		entrez = i[0].decode("utf-8")

		# check if gene has already been inserted
		geneQuery = 'match $g isa gene, has entrez-id "' + entrez + '"; get;'
		result = tx.query(geneQuery)

		try: 
			next(result)
			# If no exception, then gene symbol exists, do nothing.
		except StopIteration:
			# Gene-symbol doesn't exist, insert.
			q = 'match $a isa protein, has uniprot-id "' + uniprotid + '"; insert $g isa gene, has entrez-id "' + entrez + '";(encoding-gene:$g, encoded-protein: $a) isa gene-protein-encoding;'
			tx.query(q)
			if counter % 200 == 0:
				tx.commit()
				tx = session.transaction().write()
				print(counter)
	tx.commit()

	print('4. DisGeNET (ENTREZ) - Inserting gene-symbol to genes.')
	counter = 0
	tx = session.transaction().write()
	for d in raw_file:
		counter = counter + 1
		try: 
			next(tx.query('match $a isa gene, has gene-symbol "' + d['gene-symbol'] + '"; get;'))
			# If no exception, then gene symbol exists, do nothing.
		except StopIteration:
			# Gene-symbol doesn't exist, insert.
			q = 'match $a isa gene, has entrez-id "' + d['geneId'] + '"; insert $a has gene-symbol "' + d['gene-symbol'] + '";'
			tx.query(q)
		if counter % 200 == 0:
			tx.commit()
			tx = session.transaction().write()
			print("Inserting gene-symbol to genes: " + str(counter))
	tx.commit()

	# Commit diseases 
	diseases = []
	for i in raw_file: 
		diseases.append(i['disease-name'])
	diseases = (list(set(diseases)))

	print('4. DisGeNET (DISEASES)')
	counter = 0
	tx = session.transaction().write()
	for i in diseases:
		counter = counter + 1
		q = 'insert $b isa disease, has disease-name "' + i + '";' 
		tx.query(q)
		if counter % 200 == 0:
			tx.commit()
			tx = session.transaction().write()
		print(str(counter) + '/' + str(len(diseases)))
	tx.commit()

	print('4. DisGeNET (genes<>diseases)')
	# Commit the protein <> disease relationships 
	counter = 0
	tx = session.transaction().write()
	for i in raw_file:
		counter = counter + 1
		q = 'match $a isa gene, has entrez-id "' + i['geneId'] + '"; $b isa disease, has disease-name "' + i['disease-name'] + '"; insert (associated-gene: $a, associated-disease: $b) isa gene-disease-association;'
		print(q)
		tx.query(q)
		print(str(counter) + '/' + str(len(raw_file)))
		if counter % 500 == 0:
			tx.commit()
			tx = session.transaction().write()

	tx.commit()

# END DisGeNET
# ----------


# ----------
# 5. START HPA Tissues

if nHPA is not 0:
	print('5. HPA Tissues')

	with open('../dataset/proteinatlas/normal_tissue.tsv', 'rt', encoding='utf-8') as csvfile:
		csvreader = csv.reader(csvfile, delimiter='	')
		raw_file = []
		n = 0
		q = ''
		for row in csvreader: 
			n = n + 1
			if n is not 1:
				d = {}
				d['gene'] = row[0]
				d['tissue'] = row[2]
				raw_file.append(d)

	# Commit tissue instances first. 
	tissue = []
	for r in raw_file[:nHPA]:
		tissue.append(r['tissue'])
	tissue = (list(set(tissue)))

	tx = session.transaction().write()
	for t in tissue: 
		q = 'insert $t isa tissue, has tissue-name "' + t + '";'
		a = tx.query(q)
	tx.commit()

	new_list = []
	new_list = [dict(t) for t in {tuple(sorted(d.items())) for d in raw_file}]

	# -----
	# Commit EnsemblIDs 
	import urllib
	import urllib.request
	import ssl
	context = ssl._create_unverified_context()
	url = 'https://www.uniprot.org/uploadlists/'

	gene = []
	for g in new_list[:nHPA]:
		gene.append(g['gene'])
	gene = list(set(gene))

	for g in gene:
		q = q + '	' + g

	params = {
	'from':'ENSEMBL_ID',
	'to':'ACC',
	'format':'tab',
	'query': q
	}
	print('Sending API call.')
	data = urllib.parse.urlencode(params).encode("utf-8")
	request = urllib.request.Request(url, data)
	contact = "tklsdj@gmail.com" 
	request.add_header('User-Agent', 'Python %s' % contact)
	response = urllib.request.urlopen(request, context=context)
	page = response.read(20000000).strip()
	page = page.split(b'\n')[1:]
	page = [s.split(b'\t') for s in page]
	print('API received.')

	counter = 0
	tcounter = 0
	tx = session.transaction().write()
	for p in page:
		tcounter = tcounter + 1
		counter = counter + 1
		try: 
			q = 'match $p isa protein, has uniprot-id "' + p[2].decode("utf-8") + '";get;'
			a = tx.query(q)
			a_concept_map_answer = 	(a) 
			print(a_concept_map_answer)
			# If protein doesn't exist, next() will raise a Stopiteration() Exception. 
			try: 
				print('1')
				q = 'match $p isa protein, has uniprot-id "' + p[2].decode("utf-8") + '"; $g isa gene ;(encoded-protein: $p, encoding-gene: $g) isa gene-protein-encoding; get $g;'
				print(q)
				print('1.5')
				a = tx.query(q)
				a_concept_map_answer = next(a) 
				print('1.9')
				# If gene doesnt, then next() will return StopInteration() Exception
				q = 'match $p isa protein, has uniprot-id "' + p[2].decode("utf-8") + '"; $g isa gene; (encoded-protein: $p, encoding-gene: $g) isa gene-protein-encoding; insert $g has ensembl-id "' + p[0].decode("utf-8") + '";'
				a = tx.query(q)
				print(q)
			except Exception:
				print('2')
				# What to do when gene doesn't exist - insert new gene with ensembl id.
				q = 'match $p isa protein, has uniprot-id "' + p[2].decode("utf-8") + '"; insert $g isa gene, has ensembl-id "' + p[0].decode("utf-8") + '"; (encoded-protein: $p, encoding-gene: $g) isa gene-protein-encoding;'
				print(q)
				a = tx.query(q)
				print(q)
		except Exception:
			# What to do when protein doesn't exist - nothing.
			print('3')
			tx.commit()
			tx = session.transaction().write()
			pass
		tcounter = tcounter + 1
		print('Total count: ' + str(tcounter) + "/" + str(len(page)))
		if counter % 200 == 0:
			tx.commit()
			tx = session.transaction().write()

	# Insert tissues to genes
	counter = 0
	tcounter = 0
	tx = session.transaction().write()
	for n in new_list[:nHPA]:
		tcounter = tcounter + 1
		print('Total committed: ' + str(counter))
		print('Total count: ' + str(tcounter) + "/" + '1053331')
		try: 
			insert = 'match $a isa gene, has ensembl-id "' + n['gene'] + '"; $b isa tissue, has tissue-name "' + n['tissue'] + '"; insert (associated-gene: $a, associated-tissue: $b) isa gene-tissue-association;'	
			print(insert)
			tx.query(insert)
			counter = counter + 1
		except Exception: 
			print('Gene does not exist.')

		if counter % 400 == 0:
			tx.commit()
			tx = session.transaction().write()

	tx.commit()


# END HPA Tissues
# ----------


# -------
# 6. START Kaneko

print('6. Kaneko')

if nKan is not 0:
	with open('../dataset/kaneko/Kaneko.csv', 'rt', encoding='utf-8') as csvfile:
		csvreader = csv.reader(csvfile, delimiter='	')
		raw_file = []
		for row in csvreader: 
			d = {}
			d['uniprot-id'] = row[0]
			d['disease1'] = row[1]
			d['number_disease'] = 1
			try: 
				d['disease2'] = row[2]
				d['number_disease'] = 2
			except Exception:
				pass
			try: 
				d['disease3'] = row[3]
				d['number_disease'] = 3
			except Exception: 
				pass
			raw_file.append(d)

	new_diseases = []
	for r in raw_file[:nKan]:
		new_diseases.append(r['disease1'])
		try: 
			new_diseases.append(r['disease2'])
		except Exception:
			pass
		try: 
			new_diseases.append(r['disease3'])
		except Exception:
			pass

	tx = session.transaction().write()
	kaneko = 'insert $db isa database, has database-name "Kaneko"; '
	tx.query(kaneko)
	tx.commit()


	# Now inserting the protein <> disease connection
	tx = session.transaction().write()
	for d in raw_file[:nKan]:
		if d['number_disease'] == 1:
			insert = 'match $a isa protein, has uniprot-id "' + d['uniprot-id'] + '"; $b isa disease, has disease-name "' + d['disease1'] + '"; $db isa database, has database-name "Kaneko";  insert $pda (associated-protein: $a, associated-disease: $b) isa protein-disease-association; (ingested-data: $pda, ingested-source: $db) isa data-ingestion;'	
			tx.query(insert)		
			print(insert)
		if d['number_disease'] == 2:
			insert = 'match $a isa protein, has uniprot-id "' + d['uniprot-id'] + '"; $b isa disease, has disease-name "' + d['disease1'] + '"; $c isa disease, has disease-name "' + d['disease2'] + '"; $db isa database, has database-name "Kaneko";  insert $pda1(associated-protein: $a, associated-disease: $b) isa protein-disease-association; $pda2(associated-protein: $a, associated-disease: $c) isa protein-disease-association; (ingested-data: $pda1, ingested-source: $db) isa data-ingestion; (ingested-data: $pda2, ingested-source: $db) isa data-ingestion;'	
			tx.query(insert)			
			print(insert)
		if d['number_disease'] == 3:
			insert = 'match $a isa protein, has uniprot-id "' + d['uniprot-id'] + '"; $b isa disease, has disease-name "' + d['disease1'] + '"; $c isa disease, has disease-name "' + d['disease2'] + '"; $d isa disease, has disease-name "' + d['disease2'] + '"; $db isa database, has database-name "Kaneko"; insert $pda1(associated-protein: $a, associated-disease: $b) isa protein-disease-association; $pda2(associated-protein: $a, associated-disease: $c) isa protein-disease-association; $pda3(associated-protein: $a, associated-disease: $d) isa protein-disease-association; (ingested-data: $pda1, ingested-source: $db) isa data-ingestion; (ingested-data: $pda2, ingested-source: $db) isa data-ingestion; (ingested-data: $pda3, ingested-source: $db) isa data-ingestion;'	
			tx.query(insert)		
			print(insert)
	tx.commit()

#END Kaneko
#-------


#-------
# 7. START GEO 

print('7. GEO')

def open_geo_study(study, test):
	for t in test:
		with open('../dataset/ncbi/GSE-files/'  + study + '/' + t + '.csv','rt', encoding='utf-8') as csvfile:
			csvreader = csv.reader(csvfile, delimiter='	')
			raw_file = []
			test_list = []
			n = 0
			for row in csvreader: 
				n = n + 1
				if row[8] != '' and n != 1:
					d = {}
					d['geo-series'] = study
					d['geo-comparison-test'] = t
					d['p-value'] = row[2]
					d['entrez'] = row[8]
					test_list.append(d)
	return test_list[:nGEO]

if nGEO is not 0:

	GSE27876 = open_geo_study('GSE27876', ['MiA-SA', 'NC-MIA', 'NC-SA'])
	GSE43696 = open_geo_study('GSE43696', ['MMA-SA', 'NC-MMA', 'NC-SA'])
	GSE63142 = open_geo_study('GSE63142', ['MMA-SA', 'NC-MMA', 'NC-SA'])

	# Now get the uniprot-id for all gene names
	import urllib
	import urllib.request
	import ssl
	context = ssl._create_unverified_context()
	url = 'https://www.uniprot.org/uploadlists/'


	q = []
	for g in GSE27876:
		q.append(g['entrez'])
	for g in GSE43696:
		q.append(g['entrez'])
	for g in GSE63142:
		q.append(g['entrez'])

	entrez = ''
	n = 0
	for l in (list(set(q))): 
		n = n + 1
		entrez = l + '	' + entrez

	params = {
	'from':'P_ENTREZGENEID',
	'to':'ACC',
	'format':'tab',
	'query': entrez
	}

	print('Sending API call. Entrez-id to UniprotId')
	data = urllib.parse.urlencode(params).encode("utf-8")
	request = urllib.request.Request(url, data)
	contact = "tklsdj@gmail.com" 
	request.add_header('User-Agent', 'Python %s' % contact)
	response = urllib.request.urlopen(request, context=context)
	page = response.read(2000000).strip()
	page = page.split(b'\n')[1:]
	page = [s.split(b'\t') for s in page]
	print('API received.')


	with open('../dataset/ncbi/GEO-comparison-api-data.csv','w', encoding='utf-8') as csvfile:
		for row in page:
			l = []
			l.append(row[0].decode("utf-8"))
			l.append(' ' + row[1].decode("utf-8"))
			csv.writer(csvfile).writerow(l)

	with open('../dataset/ncbi/GEO-comparison-api-data.csv','rt', encoding='utf-8') as csvfile:
		csvreader = csv.reader(csvfile, delimiter=',')
		raw_file = []
		for row in csvreader: 
			raw_file.append(row)

	counter = 0
	tx = session.transaction().write()
	for i in raw_file:
		counter = counter + 1
		entrez = i[0]
		uniprotid = i[1][1:]

		# check if gene has already been inserted
		geneQuery = 'match $g isa gene, has entrez-id "' + entrez + '"; get;'
		result = tx.query(geneQuery)

		try: 
			next(result)
			# If no exception, then gene symbol exists, do nothing.
		except StopIteration:
			# Gene-symbol doesn't exist, insert.
			q = 'match $p isa protein, has uniprot-id "' + uniprotid + '"; insert $g isa gene, has entrez-id "' + entrez + '";(encoded-protein: $p, encoding-gene: $g) isa gene-protein-encoding;'
			b = tx.query(q)

		print('Inserting genes (entrez-id) <> protein relationships: ' + str(counter) + '/' + str(len(raw_file)))
		if counter % 100 == 0:
			tx.commit()
			tx = session.transaction().write()
			print(counter)

	tx.commit()

	GEO_queries = []

	GEO_queries.append('insert $a isa geo-series, has GEOStudy-id "GSE27876"; $b isa geo-series, has GEOStudy-id "GSE43696"; $c isa geo-series, has GEOStudy-id "GSE63142";')
	GEO_queries.append('match $27876 isa geo-series, has GEOStudy-id "GSE27876"; insert $gc isa geo-comparison, has GEOComparison-id "MiA-SA"; (compared-groups: $gc, containing-study: $27876) isa comparison;')
	GEO_queries.append('match $27876 isa geo-series, has GEOStudy-id "GSE27876"; insert $gc isa geo-comparison, has GEOComparison-id "NC-MiA"; (compared-groups: $gc, containing-study: $27876) isa comparison;')
	GEO_queries.append('match $27876 isa geo-series, has GEOStudy-id "GSE27876"; insert $gc isa geo-comparison, has GEOComparison-id "NC-SA"; (compared-groups: $gc, containing-study: $27876) isa comparison;')

	GEO_queries.append('match $43696 isa geo-series, has GEOStudy-id "GSE43696"; insert $gc isa geo-comparison, has GEOComparison-id "MMA-SA"; (compared-groups: $gc, containing-study: $43696) isa comparison;')
	GEO_queries.append('match $43696 isa geo-series, has GEOStudy-id "GSE43696"; insert $gc isa geo-comparison, has GEOComparison-id "NC-MMA"; (compared-groups: $gc, containing-study: $43696) isa comparison;')
	GEO_queries.append('match $43696 isa geo-series, has GEOStudy-id "GSE43696"; insert $gc isa geo-comparison, has GEOComparison-id "NC-SA"; (compared-groups: $gc, containing-study: $43696) isa comparison;')

	GEO_queries.append('match $63142 isa geo-series, has GEOStudy-id "GSE63142"; insert $gc isa geo-comparison, has GEOComparison-id "MMA-SA"; (compared-groups: $gc, containing-study: $63142) isa comparison;')
	GEO_queries.append('match $63142 isa geo-series, has GEOStudy-id "GSE63142"; insert $gc isa geo-comparison, has GEOComparison-id "NC-MMA"; (compared-groups: $gc, containing-study: $63142) isa comparison;')
	GEO_queries.append('match $63142 isa geo-series, has GEOStudy-id "GSE63142"; insert $gc isa geo-comparison, has GEOComparison-id "NC-SA"; (compared-groups: $gc, containing-study: $63142) isa comparison;')

	tx = session.transaction().write()
	for q in GEO_queries:
		print(q)
		tx.query(q)
	tx.commit()

	# Now commit the relationships 
	tx = session.transaction().write()
	counter = 0
	total_len = len(GSE27876) + len(GSE43696) + len(GSE63142)

	for g in [GSE27876, GSE43696, GSE63142]:
		for s in g:
			counter = counter + 1
			# Converts p-value from scientific notation to integer
			pv = format(float(s['p-value']), 'f')
			q = "match $g isa gene, has entrez-id '" + s['entrez'] + "'; $a isa geo-comparison, has GEOComparison-id '" + s['geo-comparison-test'] + "';  insert (analysed-gene: $g, conducted-analysis: $a) isa genetic-analysis, has p-value " + pv + ";"
			print(q)
			tx.query(q)		
			print('Inserting genetic-analysis rel between geo-comparisons and genes: ' + str(counter) + '/' + str(total_len))
			if counter % 500 == 0:
				tx.commit()
				tx = session.transaction().write()
	tx.commit()


# END GEO data
# -------


# -------
# 8. Drug-protein (DGIdb)

print('8. DGIdb')

with open('../dataset/dgidb/interactions.tsv','rt', encoding='utf-8') as csvfile:
	csvreader = csv.reader(csvfile, delimiter='	')
	raw_file = []
	n = 0
	for row in csvreader: 
		n = n + 1
		if n is not 1:
			d = {}
			d['entrez-id'] = row[2]
			d['interaction-type'] = row[4]
			d['drug-name'] = row[7]
			d['drug-chembl-id'] = row[8]
			raw_file.append(d)

counter = 0
tx = session.transaction().write()
for i in raw_file[:nDGI]:
	counter = counter + 1
	if i['entrez-id'] != '' and i['drug-name'] != '' and i['drug-chembl-id'] != '':
		if i['interaction-type'] == 'inhibitor':
			role = "inhibitor"
		elif i['interaction-type'] == 'antagonist':
			role = "antagonist"
		elif i['interaction-type'] == 'agonist':
			role = "antagonist"
		elif i['interaction-type'] == 'blocker':
			role = "blocker"
		else: 
			role = "inhibitor"	
		# Check if drug (drug-chembl-id) exists. If it does, do not insert new drug. 
		try: 
			# If drug does not exist, this will raise StopIteration
			print(next(tx.query('match $a isa drug, has drug-name "' + i['drug-name'] + '"; get;')))
			q = 'match $a isa gene, has entrez-id "' + i['entrez-id'] + '"; $b isa drug, has drug-name "' + i['drug-name'] + '"; insert (target-gene: $a, interacted-drug: $b) isa drug-gene-interaction;'
		except StopIteration:
			# If drug doesn't exist, then insert drug
			q = 'match $a isa gene, has entrez-id "' + i['entrez-id'] + '"; insert $b isa drug, has drug-name "' + i['drug-name'] + '", has drug-chembl-id "' + i['drug-chembl-id'] + '"; (target-gene: $a, interacted-drug: $b) isa drug-gene-interaction;'
		print(q)
		tx.query(q)		
		print('Inserting drug <> gene interactions: ' + str(counter) + '/' + str(len(raw_file)))
		if counter % 500 == 0:
			tx.commit()
			tx = session.transaction().write()
tx.commit()

# END Drug-gene (DGInd)
# -------

# -------
# 9. PPI <> Tissue relationships (TissueNet)

print('9. TissueNet')
if nTis is not 0:

	directory_in_str = '../dataset/tissuenet/'
	directory = os.fsencode(directory_in_str)

	files = []
	for file in os.listdir(directory):
		f = {}
		filename = os.fsdecode(file).encode('utf-8')
		f['filename'] = os.path.join(directory, filename)
		f['tissue'] = file[:-4]
		files.append(f)

	# tx = session.transaction().write()
	# for f in files: 
	# 	tissue_name = f['tissue'].decode("utf-8")
	# 	# Check if tissue exists, if not insert tissue.
	# 	try: 
	# 		next(tx.query('match $ti isa tissue, has tissue-name "' + tissue_name + '"; get;'))
	# 	except StopIteration: 
	# 		tx.query('insert $ti isa tissue, has tissue-name "' + tissue_name + '";')		
	# tx.commit()

	tx = session.transaction().write()
	for f in files[35:]: 
		counter = 0
		tissue_name = f['tissue'].decode("utf-8")
		if tissue_name == "seminal vesicle": 
			counter = 55963
		with open(f['filename'], 'rt', encoding='utf-8') as csvfile:
			csvreader = csv.reader(csvfile, delimiter='	')
			for row in csvreader: 
				counter = counter + 1
				if counter is not 1 and counter < nTis:
					first_gene = row[0]
					second_gene = row[1]
					# TODO: Match existing PPIs and insert them to tissue name. Right now duplicate PPIs are created.
					q = ('match $ti isa tissue, has tissue-name "' + tissue_name + '"; $g1 isa gene, has ensembl-id "'+ first_gene +'"; $g2 isa gene, has ensembl-id "' + second_gene + '"; ' +
						'(encoding-gene: $g1, encoded-protein: $pr1) isa gene-protein-encoding; (encoding-gene: $g2, encoded-protein: $pr2) isa gene-protein-encoding; insert $r ' + 
						'(interacting-protein: $pr1, interacting-protein: $pr2) isa protein-protein-interaction; (tissue-context: $ti, biomolecular-process: $r) isa process-localisation;')
					print(q)
					tx.query(q)
					print('Inserting ' + tissue_name + ' <> PPI: ' + str(counter))
					if counter % 500 == 0:
						tx.commit()
						tx = session.transaction().write()
	tx.query(q)

# -------
# END PPI <> Tissue relationships (TissueNet)


# -------
# INSERT example protein-similarities (Taken from primary source)

if sim is not 0: 
	tx = session.transaction().write()
	q = 'match $pr isa protein, has uniprot-id "P39900"; $pr2 isa protein, has uniprot-id "P09238"; insert (sequence-similar-protein: $pr, sequence-similar-protein: $pr2) isa protein-sequence-similarity;'
	tx.query(q)
	qq = 'match $pr isa protein, has uniprot-id "P08253"; $pr2 isa protein, has uniprot-id "P09238"; insert (sequence-similar-protein: $pr, sequence-similar-protein: $pr2) isa protein-sequence-similarity;'
	tx.query(qq)
	q2 = 'match $pr isa protein, has uniprot-id "P08727"; $pr2 isa protein, has uniprot-id "P05787"; insert (sequence-similar-protein: $pr, sequence-similar-protein: $pr2) isa protein-sequence-similarity;'
	tx.query(q2)
	q3 = 'match $pr isa protein, has uniprot-id "P08727"; $pr2 isa protein, has uniprot-id "P08729"; insert (sequence-similar-protein: $pr, sequence-similar-protein: $pr2) isa protein-sequence-similarity;'
	tx.query(q3)
	q4 = 'match $pr isa protein, has uniprot-id "P08727"; $pr2 isa protein, has uniprot-id "Q99456"; insert (sequence-similar-protein: $pr, sequence-similar-protein: $pr2) isa protein-sequence-similarity;'
	tx.query(q4)
	q5 = 'match $pr isa protein, has uniprot-id "Q99743"; $pr2 isa protein, has uniprot-id "O15516"; insert (sequence-similar-protein: $pr, sequence-similar-protein: $pr2) isa protein-sequence-similarity;'
	tx.query(q5)
	q6 = 'match $pr isa protein, has uniprot-id "O15055"; $pr2 isa protein, has uniprot-id "P56645"; insert (sequence-similar-protein: $pr, sequence-similar-protein: $pr2) isa protein-sequence-similarity;'
	tx.query(q6)

	tx.commit()

# END example protein-protein protein-similarities
# -------






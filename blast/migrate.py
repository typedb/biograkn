import grakn
import re
from Bio.SeqIO.FastaIO import SimpleFastaParser
from util import insert_if_non_existent


'''
    1. creates a Grakn session to talk to the 'proteins' keyspace
    2. inserts the database entity named 'UniProt'
    3. for each protein stored in target-protein-sequences.fasta, inserts the:
         - protein entity
         - species entity
         - species <> protein relationship
         - protein <> database relationship
'''
client = grakn.Grakn(uri="localhost:48555")
with client.session(keyspace="proteins") as session:
    with session.transaction(grakn.TxType.WRITE) as tx:
        # insert the database entity
        q_insert_database = 'insert $db isa database has name "uniprot";'
        print("Execute insert query: ", q_insert_database)
        tx.query(q_insert_database)
        tx.commit()
    with open("uniprot-asthma-proteins.fasta") as in_handle:
        for first_line, sequence in SimpleFastaParser(in_handle):
            with session.transaction(grakn.TxType.WRITE) as tx:
                # extra relevant data from first_line of each fasta (protein)
                protein_details = re.split(',| OS=| OX=',
                                           first_line.replace(' ', ',', 1))
                identifier = protein_details[0].split("|")[1]
                name = protein_details[1]
                species = protein_details[2]

                # insert the protein entity
                q_insert_protein = ("insert $pr isa protein " +
                                    'has identifier "' + identifier + '" ' +
                                    'has name "' + name + '" ' +
                                    'has sequence "' + sequence + '";')
                print("Execute insert query: ", q_insert_protein)
                tx.query(q_insert_protein)

                # insert the sourcing-of-information relationship
                q_insert_sourcing_of_information = ('match $pr isa protein has identifier "' + identifier + '"; ' +
                                                    '$db isa database has name "uniprot"; ' +
                                                    "insert (information-source: $db, sourced-information: $pr) isa sourcing-of-information;")
                print("Execute insert query: ",
                      q_insert_sourcing_of_information)
                tx.query(q_insert_sourcing_of_information)

                # insert the species entity (if it doesn't already exist)
                q_insert_species = 'insert $species isa species has name "' + species + '"; '
                print("Execute insert query: ", q_insert_species)
                insert_if_non_existent(session, q_insert_species, "$species")

                # insert protein-ownership relationship (protein <> species)
                q_insert_protein_ownership = ('match $species isa species has name "' + species + '"; ' +
                                              '$protein has identifier "' + identifier + '"; ' +
                                              "insert (species-owner: $species, owned-protein: $protein) isa protein-ownership;")
                print("Execute insert query: ", q_insert_protein_ownership)
                tx.query(q_insert_protein_ownership)

                tx.commit()
                print("- - - - - - - - - - - - - - - - -")

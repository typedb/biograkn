import grakn
import re
from Bio.SeqIO.FastaIO import SimpleFastaParser
from util import insert_if_non_existent, insert_anyway


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
    # insert the database entity
    q_insert_database = 'insert $db isa database has name "uniprot";'
    db_id = insert_anyway(session, q_insert_database)

    with open("uniprot-asthma-proteins.fasta") as in_handle:
        for first_line, sequence in SimpleFastaParser(in_handle):
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
            protein_id = insert_anyway(session, q_insert_protein)

            # insert the sourcing-of-information relationship
            q_insert_sourcing_of_information = ('match $pr id ' + protein_id + '; ' +
                                                '$db id ' + db_id + '; ' +
                                                "insert (information-source: $db, sourced-information: $pr) isa sourcing-of-information;")
            insert_anyway(session, q_insert_sourcing_of_information)

            # insert the species entity (if it doesn't already exist)
            q_insert_species = 'insert $species isa species has name "' + species + '"; '
            species_id = insert_if_non_existent(session,
                                                q_insert_species,
                                                "$species")

            # insert protein-ownership relationship (protein <> species)
            q_insert_protein_ownership = ('match $species id ' + species_id + '; ' +
                                          '$protein id ' + protein_id + '; ' +
                                          "insert (species-owner: $species, owned-protein: $protein) isa protein-ownership;")
            insert_anyway(session, q_insert_protein_ownership)
            print("- - - - - - - - - - - - - - - - -")

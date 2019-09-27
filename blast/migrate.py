from grakn.client import GraknClient
import re
from Bio.SeqIO.FastaIO import SimpleFastaParser
from util import insert_if_non_existent


def init(data_path):
    """
        1. creates a Grakn session to talk to the 'proteins' keyspace
        2. inserts the database entity named 'UniProt'
        3. for each protein stored in target-protein-sequences.fasta, inserts the:
            - protein entity
            - species entity
            - species <> protein relationship
            - protein <> database relationship
    """
    with GraknClient(uri="localhost:48555") as client:
        with client.session(keyspace="proteins") as session:
            # insert the database entity
            q_get_database = 'match $db isa database, has name "uniprot"; get $db;'
            q_insert_database = 'insert $db isa database, has name "uniprot";'
            database_id = insert_if_non_existent(
                session, q_get_database, q_insert_database, "$db"
            )

            with open(data_path) as data:
                for first_line, sequence in SimpleFastaParser(data):
                    # extra relevant edata from first_line of each fasta (protein)
                    protein_details = re.split(',| OS=| OX=', first_line.replace(' ', ',', 1))
                    identifier = protein_details[0].split("|")[1]
                    name = protein_details[1]
                    species = protein_details[2]

                    # insert the protein entity
                    q_get_protein = (
                        'match $pr isa protein ' +
                        ', has identifier "' + identifier + '" ' +
                        ', has name "' + name + '" ' +
                        ', has sequence "' + sequence + '"; ' +
                        'get $pr;'
                    )
                    q_insert_protein = (
                        'insert $pr isa protein ' +
                        ', has identifier "' + identifier + '" ' +
                        ', has name "' + name + '" ' +
                        ', has sequence "' + sequence + '";')
                    protein_id = insert_if_non_existent(
                        session, q_get_protein, q_insert_protein, "$pr"
                    )

                    # insert the sourcing-of-information relationship
                    q_get_sourcing_of_information = (
                        'match $pr id ' + protein_id + '; ' +
                        '$db id ' + database_id + '; ' +
                        '$sourcing (information-source: $db, sourced-information: $pr) isa sourcing-of-information; ' +
                        'get $sourcing;'
                    )
                    q_insert_sourcing_of_information = (
                        'match $pr id ' + protein_id + '; ' +
                        '$db id ' + database_id + '; ' +
                        'insert $sourcing (information-source: $db, sourced-information: $pr) isa sourcing-of-information;'
                    )
                    insert_if_non_existent(
                        session, q_get_sourcing_of_information, q_insert_sourcing_of_information, "$sourcing"
                    )

                    # insert the species entity
                    q_get_species = 'match $species isa species, has name "' + species + '"; get $species;'
                    q_insert_species = 'insert $species isa species, has name "' + species + '";'
                    species_id = insert_if_non_existent(
                        session, q_get_species, q_insert_species, "$species"
                    )

                    # insert protein-ownership relationship
                    q_get_protein_ownership = (
                        'match $species id ' + species_id + '; ' +
                        '$protein id ' + protein_id + '; ' +
                        '$pr-ownership (species-owner: $species, owned-protein: $protein) isa protein-ownership; ' +
                        'get $pr-ownership;'
                    )
                    q_insert_protein_ownership = (
                        'match $species id ' + species_id + '; ' +
                        '$protein id ' + protein_id + '; ' +
                        'insert $pr-ownership (species-owner: $species, owned-protein: $protein) isa protein-ownership;'
                    )
                    insert_if_non_existent(
                        session, q_get_protein_ownership, q_insert_protein_ownership, "$pr-ownership"
                    )


if __name__ == "__main__":
    init(data_path="uniprot-asthma-proteins.fasta")

import grakn
import re
import Bio
from Bio.Blast import NCBIWWW
from Bio.Blast import NCBIXML
from util import insert_if_non_existent, insert_anyway


'''
    runs a match query to get the target protein sequences
    :param session as dict: used to talk to the proteins keyspace
    returns the extracted sequences as a list of strings
'''

def query_target_sequences(session):
    with session.transaction(grakn.TxType.READ) as tx:
        q_match_target_sequences = 'match $p isa protein has sequence $s; limit 1; get $s;'
        print("Extracting target sequences: ", q_match_target_sequences)
        print("- - - - - - - - - - - - - - - - - - - - -")
        answers = tx.query(q_match_target_sequences).collect_concepts()
        sequences = [answer.value() for answer in answers]
        return sequences


'''
    1. inserts the databse against which the BLAST search ran
    2. for each alignment found, inserts the:
        a. protein entity
        b. sequence <> database relationship
        c. sequence <> sequence alignment relationship
        d. species entity
        e. species <> protein relationship
    :param session as dict: used to talk to the proteins keyspace
    :param target_sequence as string: the sequence whose alignments are returned from BLAST
    :param record as BLAST.Record (from Biopython): the BLAST record that contains the result from the BLAST search
'''


def insert_new_proteins_n_alignments(session, target_sequence, record):
    database = record.database
    # insert the database entity (if it doesn't already exist)
    q_insert_database = 'insert $db isa database has name "' + database + '";'
    database_id = insert_if_non_existent(session,
                                         q_insert_database,
                                         "$db")

    for alignment in record.alignments:
        # insert the protein entity (if it doesn't already exist)
        protein_name = alignment.hit_def.split(" >")[0].split(";")[0]
        q_insert_protein = 'insert $pr isa protein has name "' + protein_name + '";'
        protein_id = insert_if_non_existent(session,
                                            q_insert_protein,
                                            "$pr")

        for hsp in alignment.hsps:
            # insert the sequence attribute for the protein entity (if doesn't exists already)
            q_insert_sequence_for_protein = ('match $pr id ' + protein_id + '; ' +
                                             'insert $seq isa sequence "' + hsp.sbjct + '"; $pr has sequence $seq;')
            insert_if_non_existent(session,
                                   q_insert_sequence_for_protein,
                                   "$seq")

            # insert the sourcing-of-information relationship (if it doesn't already exist)
            q_insert_sourcing = ('match $seq isa sequence "' + hsp.sbjct + '"; ' +
                                 '$db id ' + database_id + '; ' +
                                 "insert $sourcing (information-source: $db, sourced-information: $seq) isa sourcing-of-information;")
            insert_if_non_existent(session,
                                   q_insert_sourcing,
                                   "$sourcing")

            # insert the alignment relationship (if it doesn't already exist)
            sequence_positivity = round(hsp.positives / alignment.length, 3)
            sequence_identicality = round(hsp.identities / alignment.length, 3)
            sequence_gaps = round(hsp.gaps / alignment.length, 5)
            sequence_midline = hsp.match
            alignment_identifier = alignment.hit_id.split("|", 4)[3]
            q_insert_alignment = ('match $target-seq isa sequence "' + target_sequence + '"; ' +
                                  '$matched-seq isa sequence "' + hsp.sbjct + '"; ' +
                                  'insert $alignment (target-sequence: $target-seq, matched-sequence: $matched-seq) isa sequence-sequence-alignment;')
            q_insert_alignment_attr = ('$alignment has sequence-positivity ' + str(sequence_positivity) +
                                       ' has sequence-identicality ' + str(sequence_identicality) +
                                       ' has sequence-gaps ' + str(sequence_gaps) +
                                       ' has sequence-midline "' + sequence_midline + '"' +
                                       ' has identifier "' + alignment_identifier + '";')
            insert_if_non_existent(session,
                                   q_insert_alignment,
                                   q_insert_alignment_attr)

        # insert the species entity (if it doesn't already exist)
        if (len(alignment.hit_def.split("[")) > 1):
            species = alignment.hit_def.split("[")[1].split("]")[0]
            q_insert_species = 'insert $species isa species has name "' + species + '"; '
            species_id = insert_if_non_existent(session,
                                                q_insert_species,
                                                "$species")

            # insert protein-ownership relationship (if it doesn't already exist)
            q_insert_protein_ownership = ('match $sp id "' + species_id + '"; ' +
                                          '$pr id ' + protein_id + '; ' +
                                          "insert $pr-ownership (species-owner: $sp, owned-protein: $pr) isa protein-ownership;")
            insert_if_non_existent(session,
                                   q_insert_protein_ownership,
                                   "$pr-ownership")

        print("- - - - - - - - - - - - - - - - - - - - -")


client = grakn.Grakn(uri="localhost:48555")
with client.session(keyspace="proteins") as session:
    print("Connected to the proteins knowledge graph.")
    print("- - - - - - - - - - - - - - - - - - - - -")
    target_sequences = query_target_sequences(session)

    for sequence in target_sequences:
        print("BLASTing for: ", sequence)
        print("- - - - - - - - - - - - - - - - - - - - -")
        print("Waiting for BLAST search to complete. This can take a few minutes.")
        result_handle = NCBIWWW.qblast(
            "blastp",
            "nr",
            sequence
        )
        print("Reading BLAST results")
        print("- - - - - - - - - - - - - - - - - - - - -")
        blast_record = NCBIXML.read(result_handle)
        # blast_record = NCBIXML.read(open("temp.xml"))
        print("Inserting BLAST results into the proteins knowledge graph.")
        print("- - - - - - - - - - - - - - - - - - - - -")
        insert_new_proteins_n_alignments(session, sequence, blast_record)

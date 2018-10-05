import grakn
import re
import sys
import Bio
from Bio.Blast import NCBIWWW
from Bio.Blast import NCBIXML
from util import insert_if_non_existent


'''
    gets the sequences of tatget proteins
    :param session as dict: used to talk to the proteins keyspace
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
        b. sequence attribute and its corresponding gi attribute
        c. sequence <> database relationship
        d. sequence <> sequence alignment relationship
        e. species entity
        f. species <> protein relationship
    :param session as dict: used to talk to the proteins keyspace
    :param target_sequence as string: the sequence whose alignments are returned from BLAST
    :param record as BLAST.Record (from Biopython): the BLAST record that contains the result from the BLAST search
'''


def insert_new_proteins_n_alignments(session, target_sequence, record):
    database = record.database
    # insert the database entity (if it doesn't already exist)
    q_insert_database = 'insert $db isa database has name "' + database + '";'
    insert_if_non_existent(session,
                           q_insert_database,
                           "$db")

    for alignment in record.alignments:
        title_line = alignment.title

        # insert the protein entity (if it doesn't already exist)
        protein_name = title_line.split(" ", 1)[1].split(
            "[")[0].split(">")[0].replace(";", " -")
        q_insert_protein = 'insert $pr isa protein has name "' + protein_name + '";'
        insert_if_non_existent(session,
                               q_insert_protein,
                               "$pr")

        for hsp in alignment.hsps:
            # insert the sequence attribute for the protein entity (if doesn't exists already)
            q_insert_sequence_for_protein = ('match $pr isa protein has name "' + protein_name + '"; ' +
                                             'insert $seq isa sequence "' + hsp.sbjct + '"; $pr has sequence $seq;')
            insert_if_non_existent(session,
                                   q_insert_sequence_for_protein,
                                   "$seq")

            # insert the gi attribute for the sequence attribute (if doesn't exists already)
            gi = title_line.split(" ", 1)[0].split("|")[1]
            q_insert_gi_for_sequence = ('match $seq isa sequence "' + hsp.sbjct + '"; ' +
                                        'insert $gi isa gi "' + gi + '"; $seq has gi $gi;')
            insert_if_non_existent(session,
                                   q_insert_gi_for_sequence,
                                   "$gi")

            # insert the sourcing-of-information relationship (if it doesn't already exist)
            q_insert_sourcing = ('match $seq isa sequence has gi "' + gi + '"; ' +
                                 '$db isa database has name "' + database + '"; ' +
                                 "insert $sourcing (information-source: $db, sourced-information: $seq) isa sourcing-of-information;")
            insert_if_non_existent(session,
                                   q_insert_sourcing,
                                   "$sourcing")

            # insert the alignment relationship (if it doesn't already exist)
            sequence_positivity = (hsp.positives / alignment.length)
            sequence_identicality = (hsp.identities / alignment.length)
            sequence_gaps = hsp.gaps / alignment.length
            q_insert_alignment = ('match $target-seq isa sequence "' + target_sequence + '"; ' +
                                  '$matched-seq isa sequence "' + hsp.sbjct + '"; ' +
                                  'insert $alignment (target-sequence: $target-seq, matched-sequence: $matched-seq) isa sequence-sequence-alignment;')
            q_insert_alignment_attr = ('$alignment has sequence-positivity ' + str(sequence_positivity) +
                                       ' has sequence-identicality ' + str(sequence_identicality) +
                                       ' has sequence-gaps ' + str(sequence_gaps) + ';')
            insert_if_non_existent(session,
                                   q_insert_alignment,
                                   q_insert_alignment_attr)

        # insert the species entity (if it doesn't already exist)
        print(title_line)
        if (len(title_line.split(" ", 1)[1].split("[")) > 1):
            species = title_line.split(" ", 1)[1].split("[")[1].split("]")[0]
            q_insert_species = 'insert $species isa species has name "' + species + '"; '
            insert_if_non_existent(session,
                                   q_insert_species,
                                   "$species")

            # insert protein-ownership relationship (if it doesn't already exist)
            q_insert_protein_ownership = ('match $sp isa species has name "' + species + '"; ' +
                                          '$pr isa protein has name "' + protein_name + '"; ' +
                                          "insert $pr-ownership (species-owner: $sp, owned-protein: $pr) isa protein-ownership;")
            insert_if_non_existent(session,
                                   q_insert_protein_ownership,
                                   "$pr-ownership")

        print("- - - - - - - - - - - - - - - - - - - - -")


'''
    1. for each sequence in the proteins knowledge graph:
        - if the sequence is attached to more than one protein:
            a. prints the proteins names as list
            b. asks the user for the one that need to be eleted
            c. deletes the selected proteins
    :param session as dict: used to talk to the proteins keyspace
'''


def clean_data(session):
    with session.transaction(grakn.TxType.READ) as read_tx:
        # get all sequences
        q_match_all_sequences = 'match $seq isa sequence; get $seq;'
        sequence_answers = read_tx.query(
            q_match_all_sequences).collect_concepts()
        sequences = [sequence_answer.value()
                     for sequence_answer in sequence_answers]
        is_data_clean = True

        # check for duplicate proteins
        for sequence in sequences:
            q_match_proteins_of_sequence = 'match $pr isa protein has sequence "' + \
                sequence + '" has name $name ; get $name;'
            protein_answers = read_tx.query(
                q_match_proteins_of_sequence).collect_concepts()
            protein_names = [protein_answer.value()
                             for protein_answer in protein_answers]
            may_be_dirty = len(protein_names) > 1

            if may_be_dirty:
                is_data_clean = False
                # list duplicate proteins
                print(
                    "These proteins all have the same sequence:")
                for index, protein_name in enumerate(protein_names):
                    print(str(index + 1) + ". " + protein_name + ".")

                # get user's selection
                selection_to_delete = -1
                while True:
                    selection_to_delete = input(
                        "Enter the number of proteins to be deleted, seperated by comma. (enter 0 to keep all): ")
                    are_selections_valid = True
                    for selection in selection_to_delete.strip().split(","):
                        if int(selection) < 0 or int(selection) > len(protein_names):
                            are_selections_valid = False
                            break
                    if are_selections_valid:
                        break
                print("")

                if not selection_to_delete == 0:
                    # delete selected proteins
                    for selection in selection_to_delete.strip().split(","):
                        protein_to_be_deleted = protein_names[int(
                            selection) - 1]
                        with session.transaction(grakn.TxType.WRITE) as write_tx:
                            q_delete_protein = ('match $pr isa protein has name "' +
                                                protein_to_be_deleted + '"; delete $pr;')
                            print("Execute delete query: ", q_delete_protein)
                            write_tx.query(q_delete_protein)
                            write_tx.commit()

    if is_data_clean:
        print("There are no duplicate proteins.")
    else:
        print("There are no more duplicate proteins.")


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
        print("Inserting BLAST results into the proteins knowledge graph.")
        print("- - - - - - - - - - - - - - - - - - - - -")
        insert_new_proteins_n_alignments(session, sequence, blast_record)
    print("Checking for duplicate proteins ...")
    print("- - - - - - - - - - - - - - - - - - - - -")
    clean_data(session)

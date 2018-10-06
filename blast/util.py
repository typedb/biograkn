import grakn

'''
    1. creates a match_query based of the insert_minimal_query
    2. executes the match_query
    3. if match_query returned nothing:
        a. creates the inser_query
        b. executes the inser_query
    :param session as dict: a Grakn session used to talk to the proteins keyspace
    :param insert_minimal_query as string: the Graql insert query excluding the attributes associated with the item to be inserted)
    :param insert_attributes_or_variable as string:
        - if item to be inserted has no attributes associeted with it: contains only the item's variable (example: '$sourcing')
        - otherwise: contains the variable and the attributes associated with it (example: '$sourcing has name "whatever";')
    returns the id of the item that matched or inserted
'''


def insert_if_non_existent(session, insert_minimal_query, insert_attributes_or_variable):
    # creating the match query
    if insert_minimal_query.count(";") == 1:
        match_query = insert_minimal_query.replace("insert", "match")
    else:
        match_query = insert_minimal_query.replace("insert ", "")
    match_query += " get " + insert_attributes_or_variable.split(" ")[0] + ";"

    exists = False
    with session.transaction(grakn.TxType.READ) as read_tx:
        answers = read_tx.query(match_query).collect_concepts()
        exists = len(answers) > 0

    id = None
    if exists:
        id = answers[0].id
    else:
        insert_query = insert_minimal_query
        # insert_attributes_or_variable contains attributes
        if insert_attributes_or_variable.count(";") > 0:
            insert_query += insert_attributes_or_variable
        print("Execute insert query: ", insert_query)
        with session.transaction(grakn.TxType.WRITE) as write_tx:
            id = write_tx.query(insert_query).collect_concepts()[0].id
            write_tx.commit()
    return id

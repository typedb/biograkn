import pprint

'''
Looks for the instance based on the given get_query
  - if found: returns the id of the matched instance
  - if not found:
    - inserts the instance by running the insert_query, and
    - returns the id of the inserted instance
'''

def insert_if_non_existent(session, get_query, insert_query, variable):
    variable = variable.replace("$", "")
    with session.transaction().read() as read_transaction:
        found_list = list(read_transaction.query(get_query))
        if len(found_list) > 0:
            instance_id = found_list[0].map().get(variable).id
        else:
            print("Execute insert query: ", insert_query)
            with session.transaction().write() as write_transaction:
                inserted_list = list(write_transaction.query(insert_query))
                instance_id = inserted_list[0].map().get(variable).id
                write_transaction.commit()
            print("- - - - - - - - - - - - -")
    return instance_id


'''
    prints nicely.
'''

def print_to_log(title, content, pretty=False):
    pp = pprint.PrettyPrinter(indent=4)
    print(title)
    print("")
    if pretty:
        pp.pprint(content)
    else:
        print(content)
    print("\n")

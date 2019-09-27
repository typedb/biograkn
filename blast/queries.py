from grakn.client import GraknClient
from util import print_to_log


# Which Homo Sepiens protein sequences are aligned with the sequence MNVGTAHSEVNPNTRVMNSRGIWLSYVLAIGLLHIVLLSIPFVSVPVVWTLTNLIHNMGMYIFLHTVKGTPFETPDQGKARLLTHWEQMDYGVQFTASRKFLTITPIVLYFLTSFYTKYDQIHFVLNTVSLMSVLIPKLPQLHGVRIFGINKY?
def execute_query_1(question, transaction):
    print_to_log("Question: ", question)

    query = [
        'match',
        '  $t-pr isa protein, has sequence $t-seq;',
        '  $t-seq == "MNVGTAHSEVNPNTRVMNSRGIWLSYVLAIGLLHIVLLSIPFVSVPVVWTLTNLIHNMGMYIFLHTVKGTPFETPDQGKARLLTHWEQMDYGVQFTASRKFLTITPIVLYFLTSFYTKYDQIHFVLNTVSLMSVLIPKLPQLHGVRIFGINKY";'
        '  $alignment (target-sequence: $t-seq, matched-sequence: $m-seq) isa sequence-sequence-alignment;',
        '  $alignment has sequence-identicality $ident, has sequence-positivity $pos, has sequence-midline $midline;',
        '  $species isa species, has name "Fukomys damarensis";',
        '  $ownership (owned-protein: $m-pr, species-owner: $species) isa protein-ownership;',
        'get $m-seq, $midline, $ident, $pos;'
    ]

    print_to_log("Query:", "\n".join(query))
    query = "".join(query)

    answers = transaction.query(query)

    result = []
    for structured_answer in answers:
        var_map = structured_answer.map()
        var_value_dict = {
            var_name: var_map[var_name].value() for var_name in var_map
        }
        result.append(var_value_dict)

    print_to_log("Result:", result, pretty=True)

    return result


# Which proteins sequences are aligned with the sequence MNVGTAHSEVNPNTRVMNSRGIWLSYVLAIGLLHIVLLSIPFVSVPVVWTLTNLIHNMGMYIFLHTVKGTPFETPDQGKARLLTHWEQMDYGVQFTASRKFLTITPIVLYFLTSFYTKYDQIHFVLNTVSLMSVLIPKLPQLHGVRIFGINKY and have an identicality of at lease 0.9 and a positivity of at least 0.85?
def execute_query_2(question, transaction):
    print_to_log("Question: ", question)

    query = [
        'match',
        '  $t-pr isa protein, has sequence $t-seq;',
        '  $t-seq == "MNVGTAHSEVNPNTRVMNSRGIWLSYVLAIGLLHIVLLSIPFVSVPVVWTLTNLIHNMGMYIFLHTVKGTPFETPDQGKARLLTHWEQMDYGVQFTASRKFLTITPIVLYFLTSFYTKYDQIHFVLNTVSLMSVLIPKLPQLHGVRIFGINKY";'
        '  $alignment (target-sequence: $t-seq, matched-sequence: $m-seq) isa sequence-sequence-alignment;',
        '  $alignment has sequence-identicality $ident, has sequence-positivity $pos, has sequence-midline $midline;',
        '  $ident >= 0.8; $pos >= 0.85;',
        'get $m-seq, $midline, $ident, $pos;'
    ]

    print_to_log("Query:", "\n".join(query))
    query = "".join(query)

    answers = transaction.query(query)

    result = []
    for structured_answer in answers:
        var_map = structured_answer.map()
        var_value_dict = {
            var_name: var_map[var_name].value() for var_name in var_map
        }
        result.append(var_value_dict)

    print_to_log("Result:", result, pretty=True)

    return result


# Which alignments with sequence MNVGTAHSEVNPNTRVMNSRGIWLSYVLAIGLLHIVLLSIPFVSVPVVWTLTNLIHNMGMYIFLHTVKGTPFETPDQGKARLLTHWEQMDYGVQFTASRKFLTITPIVLYFLTSFYTKYDQIHFVLNTVSLMSVLIPKLPQLHGVRIFGINKY contain the subset GIGLLHII?
def execute_query_3(question, transaction):
    print_to_log("Question: ", question)

    query = [
        'match',
        '  $t-pr isa protein, has sequence $t-seq;',
        '  $t-seq == "MNVGTAHSEVNPNTRVMNSRGIWLSYVLAIGLLHIVLLSIPFVSVPVVWTLTNLIHNMGMYIFLHTVKGTPFETPDQGKARLLTHWEQMDYGVQFTASRKFLTITPIVLYFLTSFYTKYDQIHFVLNTVSLMSVLIPKLPQLHGVRIFGINKY";'
        '  $alignment (target-sequence: $t-seq, matched-sequence: $m-seq) isa sequence-sequence-alignment;',
        '  $alignment has sequence-identicality $ident, has sequence-positivity $pos, has sequence-midline $midline;',
        '  $m-seq contains "GIGLLHII";',
        'get $m-seq, $midline, $ident, $pos;'
    ]

    print_to_log("Query:", "\n".join(query))
    query = "".join(query)

    answers = transaction.query(query)

    result = []
    for structured_answer in answers:
        var_map = structured_answer.map()
        var_value_dict = {
            var_name: var_map[var_name].value() for var_name in var_map
        }
        result.append(var_value_dict)

    print_to_log("Result:", result, pretty=True)

    return result


def execute_query_all(transaction):
    for query_example in query_examples:
        question = query_example["question"]
        query_function = query_example["query_function"]
        query_function(question, transaction)
        print("\n - - -  - - -  - - -  - - - \n")


query_examples = [
    {
        "question": "Which Homo Sepiens protein sequences are aligned with the sequence MNVGTAHSEVNPNTRVMNSRGIWLSYVLAIGLLHIVLLSIPFVSVPVVWTLTNLIHNMGMYIFLHTVKGTPFETPDQGKARLLTHWEQMDYGVQFTASRKFLTITPIVLYFLTSFYTKYDQIHFVLNTVSLMSVLIPKLPQLHGVRIFGINKY?",
        "query_function": execute_query_1
    },
    {
        "question": "Which proteins sequences are aligned with the sequence MNVGTAHSEVNPNTRVMNSRGIWLSYVLAIGLLHIVLLSIPFVSVPVVWTLTNLIHNMGMYIFLHTVKGTPFETPDQGKARLLTHWEQMDYGVQFTASRKFLTITPIVLYFLTSFYTKYDQIHFVLNTVSLMSVLIPKLPQLHGVRIFGINKY and have an identicality of at lease 0.8 and a positivity of at least 0.85?",
        "query_function": execute_query_2
    },
    {
        "question": "Which alignments with the sequence MNVGTAHSEVNPNTRVMNSRGIWLSYVLAIGLLHIVLLSIPFVSVPVVWTLTNLIHNMGMYIFLHTVKGTPFETPDQGKARLLTHWEQMDYGVQFTASRKFLTITPIVLYFLTSFYTKYDQIHFVLNTVSLMSVLIPKLPQLHGVRIFGINKY contain the subset GIGLLHII?",
        "query_function": execute_query_3
    }
]

if __name__ == "__main__":

    """
        The code below:
            - gets user's selection wrt the queries to be executed
            - creates a Grakn client > session > transaction connected to the phone_calls keyspace
            - runs the right function based on the user's selection
            - closes the session
    """

    # ask user which question to execute the query for
    print("")
    print("For which of these questions, on the blast knowledge graph, do you want to execute the query?\n")
    for index, query_example in enumerate(query_examples):
        print(str(index + 1) + ". " + query_example["question"])
    print("")

    # get user's question selection
    qs_number = -1
    while qs_number < 0 or qs_number > len(query_examples):
        try:
            qs_number = int(
                input("choose a number (0 for to answer all questions): "))
        except ValueError:
            print("Please enter valid number")
            continue
    print("")

    # open/close client, session and transaction to talk to the blast keyspace
    with GraknClient(uri="localhost:48555") as client:
        with client.session(keyspace="proteins") as session:
            with session.transaction().read() as transaction:
                # execute the query for the selected question
                if qs_number == 0:
                    execute_query_all(transaction)
                else:
                    question = query_examples[qs_number - 1]["question"]
                    query_function = query_examples[qs_number - 1]["query_function"]
                    query_function(question, transaction)

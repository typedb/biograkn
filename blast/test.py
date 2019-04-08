from grakn.client import GraknClient

import unittest

import migrate
import blast
import queries

client = GraknClient(uri="localhost:48555")
session = client.session(keyspace="blast")


class Test(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        with open('blast/schema.gql', 'r') as schema:
            define_query = schema.read()
            with session.transaction().write() as transaction:
                transaction.query(define_query)
                transaction.commit()
                print("Loaded the blast schema")

    def test_a_migration(self):
        migrate.init("blast/uniprot-asthma-proteins.fasta")

        with session.transaction().read() as transaction:
            number_of_proteins = transaction.query("match $x isa protein; get $x; count;").next().number()
            self.assertEqual(number_of_proteins, 12)

            number_of_databases = transaction.query("match $x isa database; get $x; count;").next().number()
            self.assertEqual(number_of_databases, 1)

            number_of_species = transaction.query("match $x isa species; get $x; count;").next().number()
            self.assertEqual(number_of_species, 1)

            number_of_ownership = transaction.query("match $x isa protein-ownership; get $x; count;").next().number()
            self.assertEqual(number_of_ownership, 12)

    def test_b_blast(self):
        blast.init("blast/blast-output.xml")

    def test_c_queries(self):
        with session.transaction().read() as transaction:
            queries.query_examples[0].get("query_function")("", transaction),
            queries.query_examples[1].get("query_function")("", transaction),
            queries.query_examples[2].get("query_function")("", transaction)

    @classmethod
    def tearDownClass(cls):
        client.keyspaces().delete("blast")
        print("Deleted the blast keyspace")
        session.close()
        client.close()


if __name__ == '__main__':
    unittest.main()

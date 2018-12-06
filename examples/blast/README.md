---
title: Importing BLAST results into a Grakn Knowledge Graph - an Example
keywords: blast, sequence alignment, python client, bioinformatics
tags: [example]
sidebar: documentation_sidebar
permalink: /examples/blast
folder: examples
symlink: false
---

## Importing BLAST results into a Grakn Knowledge Graph - an Example

This examples illustrates how a Grakn Knowledge Graph can be used to simplify a bioinformatician's workflow with BLAST.

## Quick Start

- Open Terminal/Command Prompt:

  - [On Windows](https://www.lifewire.com/how-to-open-command-prompt-2618089)
  - [On Mac OS X](https://www.wikihow.com/Open-a-Terminal-Window-in-Mac)
  - [On Linux](https://www.howtogeek.com/howto/22283/four-ways-to-get-instant-access-to-a-terminal-in-linux/)

- Type in `git clone git@github.com:graknlabs/biograkn.git`, press enter and wait for the cloning to complete.
- Type in `cd biograkn/examples/blast`
- [Download Grakn](http://grakn.ai/download#core)
- Unzip the downloaded file
- Type in `path-to-the-unzipped-folder/grakn server start`, press enter and wait for the Grakn Server to start.
- Type in `path-to-the-unzipped-folder/graql console --keyspace proteins --file schema.gql`
- Download and Install Python 3.7:
  - [On Windows](https://www.ics.uci.edu/~pattis/common/handouts/pythoneclipsejava/python.html)
  - [On Mac OS X](http://osxdaily.com/2018/06/13/how-install-update-python-3x-mac/)
  - [on Linux](https://docs.python-guide.org/starting/install3/linux/)
- Download and Install pip3:
  - [On Windows](https://stackoverflow.com/questions/41501636/how-to-install-pip3-on-windows)
  - [On Mac OS X](https://stackoverflow.com/questions/34573159/how-to-install-pip3-on-my-mac)
  - [On Linux](https://askubuntu.com/questions/778052/installing-pip3-for-python3-on-ubuntu-16-04-lts-using-a-proxy)
- While in the blast folder via Terminal/Command Prompt:
  - Type in `pip3 install grakn`, press enter and wait for the installation to complete.
  - Type in `pip3 install biopython`, press enter and wait for the installation to complete.
  - Type in `python3 migrate.py`, press enter and wait for the migration to complete.
  - Type in `python3 blast.py`, press enter and wait for the BLAST search to complete. This can take a few minutes.

## Adcanced Usage

Some of the following instructions assume that you have a basic knowledge of writing Python code.

### Selecting different sets of target seqeunces

The query to extract target sequences is placed as the value of `q_match_target_sequences` variable in the `query_target_sequences()` method of the `blast.py` file.

### Importing your own protein sequences

If you have already gone through the **Quick Start** secions, first you need to clean the `proteins` keyspace:

- `path-to-grakn-dist-directory(the unzipped folder)/graql console --keyspace proteins`
- `clean`
- `confirm`
- `exit`
- `path-to-grakn-dist-directory(the unzipped folder)/graql console --keyspace proteins --file path-to-cloned-biograkn-repository/examples/blast/schema.gql`

Next, you need to modify the `migrate.py` file to specify the title the file that contains the proteins and their sequences. The current code reads from a `.fasta` file exported from UniProt.
For migrating data in a different format, checkout the [migration examples](http://github.com/graknlabs/examples)

Once you are done modifying the code in `migrate.py`, `cd` into the `blast` directory and run `python3 migrate.py`

### Run BLAST requests against your own EC2 BLAST Cloud instance

The code in `blast.py` uses the `qblast()` method of the [Biopython](https://github.com/biopython/biopython) library, which currenlty does not support running BLAST requests against a BLAST Cloud instance.
However, the forked version [here](https://github.com/sorsaffari/biopython) provides such support. To use this version instead, take the following steps:

- run `git clone git@github.com:sorsaffari/biopython.git`
- run `cd biopython`
- run `pip3 uninstall biopython`
- [Install biopython from source](https://github.com/sorsaffari/biopython#installation-from-source)
- modify the `blast.py` file to:
  - change the url passed to the `qblast()` method to that of your private cloud instance
  - add `username="blast"` and `password="your instance id"` to the parameters passed to `qblast()`
- run `python3 blast.py`

### Run BLAST against NCBI BLAST+

Modify the `blast.py` file to replace the call to `qblast()` with NcbiblastxCommandline() as instructed by Biopython [here](http://biopython.org/DIST/docs/tutorial/Tutorial.html#htoc98).

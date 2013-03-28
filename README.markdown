# Clojure FMI Lectures

The lectures for the [Clojure course in FMI Sofia](http://fmi.clojure.bg/).

This repository contains both the code needed to generate the lectures as HTML and the lectures themselves (as text files, in Bulgarian).

## Usage (compiling lectures)

If you want to compile all the lectures to HTML, just execute:

    $ lein run

If you want to compile a specific lecture (for example 7), you can run:

    $ lein run 7

## Creating new lectures
 To add a new lecture you must first create the `.lecture` file for it and after that add it to the `index.yml` file.
 Adding to the `index.yml` file is simple just add a new entry in this format.


     '<lecture number>':  
        title: <title of the lecture>  
        date: <date of the lecture>  
        slug: <filename of the lecture file>  


**Warning:** The filename of the lecture file does not contain the file extension `.lecture`.


###Example
     '7': 
       title: 07. Референтни типове, част 1
       date: 2013-03-26
       slug: 07-referential-types-1

## Lecture file syntax (.lecture files)

 Lecture syntax is described(in Bulgarian) in the `SYNTAX.markdown` file.

## License

Copyright © 2013

Distributed under the Eclipse Public License, the same as Clojure.

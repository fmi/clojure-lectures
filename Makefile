# A Make file, that conforms with the web site's expectations. Running 'make'
# has to create a directory called 'compile-lectures' that will be linked by
# the site's deploy process.
all:
	rm -rf compiled-lectures
	lein run
	cp -r target/lectures compiled-lectures

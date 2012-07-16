Annotator, implemented as modules according to skeleton provided in Platform module. There are three stages implemented for annotating documents : 
* create-snapshots stage will create a snapshot of documents to be annotated, accepts an document sources url list as input.
* call-bioportal will call the bioportal annotator and create temporary result file
* annotation-renderer will renders annotation from bioportal result into desired annotation format. 


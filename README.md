# DU it better - Embedder

This is the software that is supposed to take the website content scraped by the crawler and
add it to the vector store, in this case, Neo4J

## Challenges and proposed solutions

### Limits of plain cosine similarity 
Using simple cosine similarity does not seem to work. I suppose this is because we are
taking a document and creating a single embedding for it, whereas the other words have one
embedding _per token_, which is a problem when a document contains sections that would be
have a different position in the embedding space compared with other sections of the same
document. For example: let's say a document talks about billing and about how to activate /
deactivate a certain offer from du: each token in the document has an embedding which
allows to put each term in its correct place in the embedding space, whereas we create one
embedding for the whole document that is covering both billing and activation/deactivation:
my hypothesis is that this is why simple cosine similarity does not work

### A better approach: comparing the questions coming from the user to the questions in the knowledge base

A lot of the du webpages contain questions and answers pairs, my idea is to extract them
and when the user asks a question we compare it against the questions contained in the
du webpages. I haven't been able to try yet, but I think this approach should work better
because

1) Questions tends to be shorter, and have less stopwords that can cause noise in the embedding
2) Questions tend to cover one topic, so it should be easier to create an embedding for them

I will continue to work on this and see if my approach is valid

At the moment I am using Llama 3.1 8B Instruct to extract question/answers pairs. The results
are not always good. I had to write some logic to deal with the LLM wrongly splitting
questions and answers. Maybe I should use regexes, but I think I will have to use the LLM and 
deal with the errors later
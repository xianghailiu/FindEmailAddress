# FindEmailAddress
Find all email addresses from a web page

This small program demonstrates how to find all email addresses from a web domain. It not only finds emails from home page, but also crawl down to all sub pages to find emails embedded in the sub-pages. 

Here are the steps to compile and run the program:
1) Download the FindEmailAddress.java
2) Compile the class file:    javac FindEmailAddress.java
3) Build runnable jar:    jar cvfe FindEmailAddress.jar FindEmailAddress FindEmailAddress.class
4) Run the program:    java -jar FindEmailAddress.jar www.example.com

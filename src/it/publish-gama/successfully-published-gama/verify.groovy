File gamafile = new File(basedir,"target/successfully-published-gama-1.0-SNAPSHOT.gama");
assert gamafile.exists() && gamafile.isFile();

File buildlog = new File(basedir,"build.log");

assert buildlog.isFile();
assert buildlog.text.contains("deployed successfully.");
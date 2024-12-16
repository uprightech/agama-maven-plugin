File flowsdir = new File(basedir,"src/main/resources/agama/flows/");
assert flowsdir.isDirectory();
File [] flows = flowsdir.listFiles( (f)-> {
    int extpos = f.getName().lastIndexOf(".");
    if (extpos == -1) {
        return false;
    }
    String ext = f.getName().substring(extpos+1);
    return "flow".equals(ext);
});

assert flows == null || (flows.length == 0);
File outputfile = new File("target/missing-flows-1.0-SNAPSHOT.gama");
assert !outputfile.isFile();

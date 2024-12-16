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

assert flows != null && flows.length != 0;

File webassetsdir = new File(basedir,"src/main/resources/agama/web")
assert webassetsdir.isDirectory();
File [] webassets = webassetsdir.listFiles();
assert webassets != null && webassets.length != 0 ;

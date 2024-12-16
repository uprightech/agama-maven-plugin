assert new File(basedir,"README.md").exists();
assert new File(basedir,"LICENSE").exists();
assert new File(basedir,"src/main/resources/agama/project.json").exists();

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


File webassetsdir = new File(basedir,"src/main/resources/agama/web");
assert webassetsdir.isDirectory();

assert new File(basedir,"target/full-gama-1.0-SNAPSHOT.gama").exists();
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

assert flows != null;
assert flows.length != 0;
for(File flow: flows) {
    String jsfilename =  flow.getName().substring(0,flow.getName().lastIndexOf(".")) + ".js";
    final File jsfilepath = new File(basedir,"target/agama/js/"+jsfilename);
    assert jsfilepath.isFile();
}

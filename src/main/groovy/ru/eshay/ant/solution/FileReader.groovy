package ru.eshay.ant.solution

class FileReader {
    Map<String, Map<String, List<File>>> allTasks = [:]

    FileReader(String path) {
        File dir = new File(path)
        if (dir.isDirectory()) {
            List<File> files = dir.listFiles()

            files.forEach { file ->
                if (file.isDirectory()) {
                    Map<String, List<File>> fileNames = [:]
                    if (file.isDirectory()) {
                        file.listFiles().each {
                            String fileName = it.name.split("\\.").first()
                            if (fileNames.containsKey(fileName)) {
                                fileNames.put(fileName, [fileNames.get(fileName).first(), it])
                            } else {
                                fileNames.put(fileName, [it])
                            }
                        }
                    }
                    allTasks.put(file.name, fileNames)
                }
            }
        }
    }
}

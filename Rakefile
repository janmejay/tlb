task :default => [:test]

task :test do
  classpath = [File.join(".", "lib", "ant-1.7.1.jar"),
               File.join(".", "lib", "ant-junit-1.7.1.jar"), 
               File.join(".", "vendor", "ant-launcher-1.7.1.jar"),
               File.join(ENV["JAVA_HOME"], "lib", "tools.jar")].join(File::PATH_SEPARATOR)
  exec "java -cp #{classpath} org.apache.tools.ant.Main -emacs all"
end

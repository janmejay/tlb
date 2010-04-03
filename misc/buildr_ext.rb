def jars_from dest
  Dir.glob(File.expand_path(File.join(File.dirname(__FILE__), "..", dest, "*.jar")))
end

if ENV['load_balance'] == 'true'
  Buildr::JUnit.class_eval do
    def run(tests, dependencies) #:nodoc:
      # Use Ant to execute the Junit tasks, gives us performance and reporting.
      Buildr.ant('junit') do |ant|
        case options[:fork]
        when false
          forking = {}
        when :each
          forking = { :fork=>true, :forkmode=>'perTest' }
        when true, :once
          forking = { :fork=>true, :forkmode=>'once' }
        else
          fail 'Option fork must be :once, :each or false.'
        end
        mkpath task.report_to.to_s

        taskdef = Buildr.artifact(JUnit.ant_taskdef)
        taskdef.invoke
        tlb_classes = File.expand_path(File.join(File.dirname(__FILE__), "..", "target", "classes"))
        ant.path :id => 'tlb.class.path' do
          ant.pathelement :path => tlb_classes
          ant.filelist :dir => File.expand_path(File.join(File.dirname(__FILE__), "..")), :files => dependencies.join(",")
        end
        dependencies << tlb_classes
        ant.typedef :name=>'load_balanced_fileset', :classname => 'com.github.tlb.ant.LoadBalancedFileSet', :classpathref => 'tlb.class.path'
        ant.taskdef :name=>'junit', :classname=>'org.apache.tools.ant.taskdefs.optional.junit.JUnitTask', :classpath=> taskdef.to_s

        ant.junit forking.merge(:clonevm=>options[:clonevm] || false, :dir=>task.send(:project).path_to) do
          ant.classpath :path=> dependencies.join(File::PATH_SEPARATOR)
          (options[:properties] || []).each { |key, value| ant.sysproperty :key=>key, :value=>value }
          (options[:environment] || []).each { |key, value| ant.env :key=>key, :value=>value }
          Array(options[:java_args]).each { |value| ant.jvmarg :value=>value }
          ant.formatter :type=>'plain'
          ant.formatter :type=>'plain', :usefile=>false # log test
          ant.formatter :type=>'xml'
          ant.batchtest :todir=>task.report_to.to_s, :failureproperty=>'failed' do
            ant.load_balanced_fileset(:dir => 'target/tlb/test/classes', :includes => "**/*Test.class")
            ant.formatter :classname=> "com.github.tlb.ant.JunitDataRecorder"
            ant.formatter :type=>'plain'
          end
        end
        return tests unless ant.project.getProperty('failed')
      end
      # But Ant doesn't tell us what went kaput, so we'll have to parse the test files.
      tests.inject([]) do |passed, test|
        report_file = File.join(task.report_to.to_s, "TEST-#{test}.txt")
        if File.exist?(report_file)
          report = File.read(report_file)
          # The second line (if exists) is the status line and we scan it for its values.
          status = (report.split("\n")[1] || '').scan(/(run|failures|errors):\s*(\d+)/i).
            inject(Hash.new(0)) { |hash, pair| hash[pair[0].downcase.to_sym] = pair[1].to_i ; hash }
          passed << test if status[:failures] == 0 && status[:errors] == 0
        end
        passed
      end
    end

    namespace 'junit' do
      desc "Generate JUnit tests report in #{report.target}"
      task('report') do |task|
        report.generate Project.projects
        info "Generated JUnit tests report in #{report.target}"
      end
    end
  end
end

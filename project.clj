(defproject scraper "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :main scraper.core
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [cheshire "5.10.0"]
                 [clj-http "3.10.0"]
                 [environ "1.1.0"]
                 [hickory "0.7.1"]]
  :plugins [[lein-ancient "0.6.15"]
            [lein-bikeshed "0.5.2"]
            [lein-kibit "0.1.8"]]
  :uberjar-name "scraper.jar"
  :repl-options {:init-ns scraper.core})

(ns scraper.core
  (:gen-class)
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [clojure.string :as str]
            [clojure.tools.cli :as cli]
            [environ.core :refer [env]]))

(defn ^:private get-email [] {:post [(env :jira-email)]} (env :jira-email))
(defn ^:private get-token [] {:post [(env :jira-token)]} (env :jira-token))
(defn ^:private get-org [] {:post [(env :jira-org)]} (env :jira-org))

(defn ^:private base-url [] (format "https://%s.atlassian.net" (get-org)))
(defn ^:private backlog-url []
  (str (base-url) "/rest/agile/1.0/board/25/backlog"))
(defn ^:private search-url []
  (str (base-url) "/rest/api/2/search"))
(defn ^:private issue-ui-url [issue-name]
  (str (base-url) "/browse/" issue-name))

(defn ^:private get-auth [] [(get-email) (get-token)])

(defn ^:private jira-query [verb url params auth]
  (({:get http/get, :post http/post} verb)
   url
   {({:get :query-params, :post :form-params} verb) params
    :basic-auth auth}))

(defn get-backlog-issues []
  (->> (jira-query :get
                   (backlog-url)
                   {:startAt 0, :maxResults 9999}
                   (get-auth))
       :body
       json/parse-string
       clojure.walk/keywordize-keys
       :issues))

(defn run-jql [jql]
  (->> (jira-query :get
                   (search-url)
                   {:jql jql
                    :maxResults 10000
                    :startAt 0}
                   (get-auth))
       :body
       json/parse-string
       clojure.walk/keywordize-keys
       :issues))

(def sort-order {"Queue" 1
                 "Sized" 2
                 "Blocked" 3
                 "In Progress" 4
                 "Ready for Release" 5
                 "Done" 6
                 "Rejected" 7})

;; Adapted from https://github.com/clojure/tools.cli#example-usage

(def cli-options
  [["-o" "--output OUTPUT_FORMAT" "Output format - csv or org."
    :default :csv
    :default-desc ""
    :parse-fn keyword
    :validate [#{:csv :org} (str "Invalid output format. Supported "
                                 "output formats are csv and org.")]]
   ["-h" "--help"]])

(defn usage [options-summary]
  (str/join \newline
            ["Run Jira Query Language (JQL) against your jira organization"
             ""
             "Usage: lein run [OPTIONS] JQL ..."
             ""
             "Options:"
             options-summary]))

(defn validate-args [args]
  (let [{:keys [options arguments summary errors]}
        (cli/parse-opts args cli-options)]
    (cond (:help options) {:exit-message (usage summary) :ok? true}
          errors {:exit-message (str/join \newline errors)}
          :else {:options options :arguments arguments})))

(defn -main [& args]
  (let [{:keys [options arguments ok? exit-message]} (validate-args args)]
    (if exit-message
      (do (println exit-message)
          (System/exit (if ok? 0 1)))
      (let [org? (= :org (:output options))
            jql arguments
            query (str/join \space jql)
            results (->> (run-jql query)
                         (map (juxt :key
                                    (comp :name :status :fields)
                                    (comp :name :issuetype :fields)
                                    (comp :summary :fields)
                                    (comp issue-ui-url :key)))
                         (sort-by (comp sort-order second)))
            lines (if org?
                    (->> results
                         (concat [["Ticket" "State" "Type" "Summary" "Link"]
                                  [""       ""      ""     ""        "<10>"]])
                         (partition-by second)
                         (interleave (repeat [["---"]]))
                         (#(concat % [[["---"]]]))
                         (apply concat)
                         (map (partial str/join "|"))
                         (map (partial format "|%s|")))
                    (->> results
                         (concat [["Ticket" "State" "Type" "Summary" "Link"]])
                         (map (partial map (partial format "\"%s\"")))
                         (map (partial str/join ","))))]
        (->> lines
             (str/join \newline)
             println)))))

(comment
  (def issues (get-backlog-issues))

  (search-jql "project = 'Backend Wares'")
  )

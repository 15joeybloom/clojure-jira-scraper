(ns scraper.core
  (:require [clj-http.client :as http]
            [environ.core :refer [env]]
            [cheshire.core :refer [parse-string]]))

(defn ^:private get-email [] {:post [(env :jira-email)]} (env :jira-email))
(defn ^:private get-token [] {:post [(env :jira-token)]} (env :jira-token))
(def ^:private jira-issue-url
  "https://opploans.atlassian.net/rest/agile/1.0/board/25/backlog")

(defn ^:private get-auth [] [(get-email) (get-token)])

(defn ^:private jira-query [url params auth]
  (http/get url {:query-params params, :basic-auth auth}))

(defn- get-issues []
  (->> (jira-query jira-issue-url {:startAt 0, :maxResults 9999} (get-auth))
       :body
       parse-string
       clojure.walk/keywordize-keys
       :issues))

(comment
  (get-issues))

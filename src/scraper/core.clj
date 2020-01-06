(ns scraper.core
  (:require [clj-http.client :as http]
            [environ.core :refer [env]]
            [cheshire.core :refer [parse-string]]))

(defn ^:private get-email [] {:post [(env :jira-email)]} (env :jira-email))
(defn ^:private get-token [] {:post [(env :jira-token)]} (env :jira-token))
(defn ^:private get-org [] {:post [(env :jira-org)]} (env :jira-org))
(defn ^:private base-url [] (format "https://%s.atlassian.net" (get-org)))
(defn ^:private jira-issue-url []
  (str (base-url) "/rest/agile/1.0/board/25/backlog"))

(defn ^:private get-auth [] [(get-email) (get-token)])

(defn ^:private jira-query [verb url params auth]
  (({:get http/get, :post http/post} verb)
   url
   {({:get :query-params, :post :form-params} verb)
    params :basic-auth auth}))

(defn get-issues []
  (->> (jira-query :get
                   (jira-issue-url)
                   {:startAt 0, :maxResults 9999}
                   (get-auth))
       :body
       parse-string
       clojure.walk/keywordize-keys
       :issues))

(defn search-jql [jql]
  (->> (jira-query :get
                   (str (base-url) "/rest/api/2/search")
                   {:jql jql
                    :maxResults 10000
                    :startAt 0}
                   (get-auth))
       :body
       parse-string
       clojure.walk/keywordize-keys
       :issues))

(comment
  (get-issues)

  (search-jql "project = 'Backend Wares'")
  )

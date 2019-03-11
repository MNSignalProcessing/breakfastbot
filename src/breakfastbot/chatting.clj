(ns breakfastbot.chatting
  (:require [clojure.core.async :as a]
            [clojure.tools.logging :refer [info debug]]
            [java-time :as jt]
            [clojure-zulip.core :as zulip]
            [mount.core :refer [defstate]]
            [breakfastbot.config :refer [config]]))

(defstate zulip-conn
  :start (zulip/connection (:zulip config)))

(defstate zulip-event-channels
  :start (let [register-response (zulip/sync* (zulip/register zulip-conn))]
           (info "Registered to zulip server event queue")
           (zulip/subscribe-events zulip-conn register-response))
  :stop (a/>!! (second zulip-event-channels) :stop))

(defn transmit-event
  "Passes event content and author to receiver and forwards output to sender in
  Zulip, either in public or private depending on where the message originated."
  [event receiver]
  (let [message (:message event)
        {stream :display_recipient
         message-type :type
         sender :sender_email
         :keys [:subject :content]} message]
    ;; ignore messages sent by bot itself
    (if (not= (-> config :zulip :username) sender)
      (when-let [reply (receiver sender content)]
        ;; Reply to private message in private
        (if (= message-type "private")
          (zulip/send-private-message zulip-conn sender reply)
          ;; otherwise post to stream
          (zulip/send-stream-message zulip-conn stream subject reply))))))

(defn date->subject [date] (str "Breakfast " (jt/format "d.M.yyyy" date)))

(defn add-sync-handler [async-src sync-receiver]
  (let [kill-channel (a/chan)]
    (a/go-loop []
      (let [[msg channel] (a/alts! [kill-channel async-src] :priority true)]
        (cond (= channel kill-channel) (info "Async handler closing")
              :else (do (sync-receiver msg)
                        (recur)))))
    kill-channel))

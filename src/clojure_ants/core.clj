(ns clojure-ants.core
  (:require [clojure.java.io :as io])
  (:import [javafx.application Application]
           [javafx.fxml FXMLLoader]
           [javafx.stage Stage StageBuilder]
           [javafx.scene Scene]
           [javafx.scene.paint Color]
           [javafx.animation AnimationTimer])
  (:gen-class :extends javafx.application.Application))

(def width 800)
(def height 600)
(def ant-count 100)

(def ants (atom []))

(defn create-ants []
  (for [i (range 0 ant-count)]
    {:x (rand-int width)
     :y (rand-int height)
     :color (Color/BLACK)}))

(defn draw-ants [context]
  (.clearRect context 0 0 width height)
  (doseq [ant (deref ants)]
    (.setFill context (:color ant))
    (.fillOval context (:x ant) (:y ant) 5 5)))

(defn random-step []
  (- (* 2 (rand)) 1))

(defn move-ant [ant]
  (Thread/sleep 1)
  (assoc ant 
    :x (+ (random-step) (:x ant))
    :y (+ (random-step) (:y ant))))
  
(declare aggravate-ant)

(defn move-ants []
  (doall (pmap aggravate-ant (pmap move-ant (deref ants)))))

(def last-timestamp (atom 0))

(defn fps [current-timestamp]
  (let [diff (- current-timestamp (deref last-timestamp))
        diff (/ diff 1000000000)]
    (int (/ 1 diff))))

(defn aggravate-ant [ant]
  (let [nearby-ants (filter
                      (fn [ant2]
                        (and
                             (<= (Math/abs (- (:x ant) (:x ant2))) 10)
                             (<= (Math/abs (- (:y ant) (:y ant2))) 10)))
                      (deref ants))]
    (if (> (count nearby-ants) 1)
      (assoc ant :color Color/RED)
      (assoc ant :color Color/BLACK))))
    
     
      

(defn -start [app stage]
  (let [root (FXMLLoader/load (io/resource "main.fxml"))
        scene (Scene. root width height)
        canvas (.lookup scene "#canvas")
        fps-label (.lookup scene "#fps")
        context (.getGraphicsContext2D canvas)
        timer (proxy [AnimationTimer] []
                (handle [now]
                  (.setText fps-label (str (fps now)))
                  (reset! last-timestamp now)
                  (reset! ants (move-ants))
                  (draw-ants context)))]
    (doto stage
      (.setTitle "Ants")
      (.setScene scene)
      (.show))
    (reset! ants (create-ants))
    (.start timer)))

(defn -main [& args]
  (Application/launch clojure_ants.core (into-array String args)))

(defn dev-main []
  (-main))


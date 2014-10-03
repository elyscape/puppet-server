(ns puppetlabs.services.ca.certificate-authority-disabled-test
  (:require [clojure.test :refer :all]
            [me.raynes.fs :as fs]
            [puppetlabs.services.ca.certificate-authority-disabled-service :as disabled]
            [puppetlabs.services.jruby.testutils :as jruby-testutils]
            [puppetlabs.services.jruby.jruby-puppet-service :as jruby]
            [puppetlabs.services.puppet-profiler.puppet-profiler-service :as profiler]
            [puppetlabs.trapperkeeper.app :as tk-app]
            [puppetlabs.trapperkeeper.testutils.logging :as logutils]
            [puppetlabs.trapperkeeper.testutils.bootstrap :as tk-testutils]))

(def puppet-conf-dir
  "./target/test/certificate_authority_disabled_test")

(use-fixtures :each (jruby-testutils/with-puppet-conf-dir puppet-conf-dir))

(deftest ca-disabled-files-test
  (testing "Ensure no certificates are generated when CA disabled service is enabled."
    (logutils/with-test-logging
      (tk-testutils/with-app-with-config
        app

        [profiler/puppet-profiler-service
         jruby/jruby-puppet-pooled-service
         disabled/certificate-authority-disabled-service]

        (-> (jruby-testutils/jruby-puppet-tk-config
              (jruby-testutils/jruby-puppet-config 1))
            (assoc-in [:jruby-puppet :master-conf-dir]
                      puppet-conf-dir))

        (let [jruby-service (tk-app/get-service app :JRubyPuppetService)]
          (jruby/with-jruby-puppet
            jruby-puppet jruby-service
            (let [ssl-dir (str puppet-conf-dir "/ssl")]
              (is (not (nil? (fs/list-dir ssl-dir))))
              (is (empty? (fs/list-dir (str ssl-dir "/ca")))))))))))



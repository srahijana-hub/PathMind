<script setup lang="ts">
import CTASection from './landing/CTASection.vue';
import FeatureSection from './landing/FeatureSection.vue';
import FooterSection from './landing/FooterSection.vue';
import HeaderNav from './landing/HeaderNav.vue';
import HeroSection from './landing/HeroSection.vue';
import ScenarioSection from './landing/ScenarioSection.vue';
import StatsSection from './landing/StatsSection.vue';
import WorkflowSection from './landing/WorkflowSection.vue';
</script>

<template>
  <div class="landing-page">
    <HeaderNav />
    <main>
      <HeroSection />
      <FeatureSection />
      <StatsSection />
      <WorkflowSection />
      <ScenarioSection />
      <CTASection />
    </main>
    <FooterSection />
  </div>
</template>

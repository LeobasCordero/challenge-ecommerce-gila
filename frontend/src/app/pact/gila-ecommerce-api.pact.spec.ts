import { pactWith } from 'jest-pact';
import { Matchers } from '@pact-foundation/pact';
import axios from 'axios';

const { like, eachLike, term } = Matchers;

const BEARER_TOKEN = 'Bearer test-token';
const PRODUCT_ID = '550e8400-e29b-41d4-a716-446655440000';

const PACT_OPTIONS = {
  consumer: 'GilaAngularConsumer',
  provider: 'GilaECommerceAPI',
  dir: 'pacts',
  logDir: 'pact-logs',
  logLevel: 'warn' as const,
  spec: 2,
};

/** Interaction 1: Get all products */
pactWith({ ...PACT_OPTIONS, port: 1234 }, (provider) => {
  describe('GET /api/v1/products — a request to list all products', () => {
    beforeEach(() =>
      provider.addInteraction({
        state: 'products exist',
        uponReceiving: 'a request to list all products',
        withRequest: {
          method: 'GET',
          path: '/api/v1/products',
          headers: { Authorization: term({ generate: BEARER_TOKEN, matcher: 'Bearer .+' }) },
        },
        willRespondWith: {
          status: 200,
          headers: { 'Content-Type': 'application/json' },
          body: eachLike({
            id: like(PRODUCT_ID),
            name: like('Test Product'),
            category: like('Electronics'),
            price: like(9.99),
            stock: like(100),
          }),
        },
      })
    );

    it('returns a list of products', async () => {
      const response = await axios.get(`${provider.mockService.baseUrl}/api/v1/products`, {
        headers: { Authorization: BEARER_TOKEN },
      });
      expect(response.status).toBe(200);
      expect(Array.isArray(response.data)).toBe(true);
      expect(response.data[0]).toHaveProperty('name');
    });
  });
});

/** Interaction 2: Get cart for authenticated user */
pactWith({ ...PACT_OPTIONS, port: 1235 }, (provider) => {
  describe('GET /api/v1/cart — a request to get the authenticated user cart', () => {
    beforeEach(() =>
      provider.addInteraction({
        state: 'cart has items',
        uponReceiving: 'a request to get the authenticated user cart',
        withRequest: {
          method: 'GET',
          path: '/api/v1/cart',
          headers: { Authorization: term({ generate: BEARER_TOKEN, matcher: 'Bearer .+' }) },
        },
        willRespondWith: {
          status: 200,
          headers: { 'Content-Type': 'application/json' },
          body: {
            items: eachLike({
              product: like({
                id: like(PRODUCT_ID),
                name: like('Test Product'),
                price: like(9.99),
              }),
              quantity: like(2),
            }),
          },
        },
      })
    );

    it('returns the cart with items', async () => {
      const response = await axios.get(`${provider.mockService.baseUrl}/api/v1/cart`, {
        headers: { Authorization: BEARER_TOKEN },
      });
      expect(response.status).toBe(200);
      expect(response.data).toHaveProperty('items');
    });
  });
});

/** Interaction 3: Update cart item quantity */
pactWith({ ...PACT_OPTIONS, port: 1236 }, (provider) => {
  describe('POST /api/v1/cart — a request to update cart item quantity', () => {
    beforeEach(() =>
      provider.addInteraction({
        state: 'cart item can be updated',
        uponReceiving: 'a request to update cart item quantity',
        withRequest: {
          method: 'POST',
          path: '/api/v1/cart',
          headers: {
            Authorization: term({ generate: BEARER_TOKEN, matcher: 'Bearer .+' }),
            'Content-Type': 'application/json',
          },
          body: { productId: like(PRODUCT_ID), quantity: like(3) },
        },
        willRespondWith: {
          status: 200,
          headers: { 'Content-Type': 'application/json' },
          body: {
            items: eachLike({
              product: like({ id: like(PRODUCT_ID), name: like('Test Product'), price: like(9.99) }),
              quantity: like(3),
            }),
          },
        },
      })
    );

    it('updates the cart item and returns updated cart', async () => {
      const response = await axios.post(
        `${provider.mockService.baseUrl}/api/v1/cart`,
        { productId: PRODUCT_ID, quantity: 3 },
        { headers: { Authorization: BEARER_TOKEN, 'Content-Type': 'application/json' } }
      );
      expect(response.status).toBe(200);
      expect(response.data).toHaveProperty('items');
    });
  });
});

/** Interaction 4: Checkout order */
pactWith({ ...PACT_OPTIONS, port: 1237 }, (provider) => {
  describe('POST /api/v1/orders/checkout — a request to complete checkout', () => {
    beforeEach(() =>
      provider.addInteraction({
        state: 'cart is ready for checkout',
        uponReceiving: 'a request to complete checkout',
        withRequest: {
          method: 'POST',
          path: '/api/v1/orders/checkout',
          headers: { Authorization: term({ generate: BEARER_TOKEN, matcher: 'Bearer .+' }) },
        },
        willRespondWith: {
          status: 200,
          headers: { 'Content-Type': 'application/json' },
          body: {
            id: like('550e8400-e29b-41d4-a716-446655440099'),
            status: like('PAID'),
            totalPrice: like(19.98),
            items: eachLike({
              product: like({
                id: like(PRODUCT_ID),
                name: like('Test Product'),
                price: like(9.99),
              }),
              quantity: like(2),
              priceAtPurchase: like(9.99),
            }),
          },
        },
      })
    );

    it('processes checkout and returns order details', async () => {
      const response = await fetch(`${provider.mockService.baseUrl}/api/v1/orders/checkout`, {
        method: 'POST',
        headers: { Authorization: BEARER_TOKEN },
      });
      const body = await response.json() as Record<string, unknown>;
      expect(response.status).toBe(200);
      expect(body['status']).toBe('PAID');
      expect(body).toHaveProperty('totalPrice');
    });
  });
});

/** Interaction 5: Admin clear all orders */
pactWith({ ...PACT_OPTIONS, port: 1238 }, (provider) => {
  describe('DELETE /api/v1/orders/clear — an admin request to clear all orders and restore stock', () => {
    beforeEach(() =>
      provider.addInteraction({
        state: 'orders exist',
        uponReceiving: 'an admin request to clear all orders and restore stock',
        withRequest: {
          method: 'DELETE',
          path: '/api/v1/orders/clear',
          headers: { Authorization: term({ generate: BEARER_TOKEN, matcher: 'Bearer .+' }) },
        },
        willRespondWith: {
          status: 204,
        },
      })
    );

    it('clears all orders and returns 204', async () => {
      const response = await axios.delete(`${provider.mockService.baseUrl}/api/v1/orders/clear`, {
        headers: { Authorization: BEARER_TOKEN },
      });
      expect(response.status).toBe(204);
    });
  });
});
